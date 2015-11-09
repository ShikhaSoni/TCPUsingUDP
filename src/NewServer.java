/**
 * @author Shikha Soni
 * 
 * This is the server class that handles the congestion control and flow control 
 */
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;


public class NewServer {
	Queue<TCPHeader> packetsInLine = new LinkedList<TCPHeader>();
	private DatagramSocket serverSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private HashMap<Integer, TCPHeader> packets = new HashMap<Integer, TCPHeader>();
	private FileRead file;
	byte[] receiveData = new byte[1024];
	byte[] sendData = new byte[1024];
	InetAddress IPAddress;
	int port, totalPackets, ssthresh, packetToSend;
	double cwnd = 1.0;

	/**
	 * Constructor initializes the file that is to be transferred
	 */
	public NewServer(String fileName) {
		file = new FileRead(fileName);
		try {
			System.out.println("My IP: "
					+ InetAddress.getLocalHost().getHostAddress());
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		System.out.println("My port: 7999");
		try {
			serverSocket = new DatagramSocket(7999);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}
	/**
	 * This function starts the system by initializing the receiver that waits for any client connections
	 */
	public void startSystem() {
		Receiver receiver = new Receiver();
		receiver.start();
	}

	public static void main(String[] args) {
		NewServer server = new NewServer(args[0]);
		server.makePackets();
		server.startSystem();
	}

	/**
	 * This fucntion returns an object from the byte format
	 * @param packet The byte array of the serialized packet
	 * @return The actual TCPHeader object extracted from the byte array 
	 */
	public TCPHeader getPacketObject(byte[] packet) {
		TCPHeader tcp = null;
		ByteArrayInputStream b = new ByteArrayInputStream(packet);
		ObjectInputStream o;
		try {
			o = new ObjectInputStream(b);
			tcp = (TCPHeader) o.readObject();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return tcp;
	}

	/**
	 * Make Packets divides the given file into packets and gives this payload a header
	 */
	public void makePackets() {
		HashMap<Integer, byte[]> parts = file.readNext();
		this.totalPackets = file.totalPackets;
		System.out.println("Total Packets made: "+totalPackets);
		TCPHeader header;
		for (int packet_num = 1; packet_num <= totalPackets; packet_num++) {
			header = new TCPHeader();
			header.setSeqNum(packet_num);
			header.setData(parts.get(packet_num));
			header.setCheckSum(getCheckSum(header));
			//System.out.println(getCheckSum(header));
			if (packet_num == totalPackets) {
				header.setLastBit(true);
			}
			packets.put(packet_num, header);
		}
	}

	/**
	 * This method does exactly the opposite of getPacketObject
	 * Converts an object into byte array
	 * @param packet Object to be converted into bytes
	 * @return the byte array of the object
	 */
	public byte[] getPacketBytes(TCPHeader packet) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		try {
			ObjectOutputStream o = new ObjectOutputStream(b);
			o.writeObject(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b.toByteArray();
	}

	/**
	 * This method makes the checksum out of the packet
	 * @param data byte data 
	 * @return calculated checksum
	 */
	public String getCheckSum(TCPHeader object) {
		byte[] a=object.getData();
		String checkSum="";
		for(int i=0;i<2;i++){
			Byte b=a[i];
			checkSum+=b.intValue();
		}
		return checkSum;
	}

	/**
	 * 
	 * @author Shikha Soni
	 *
	 */
	class Receiver extends Thread {
		boolean firstTime = true, restart = false,
				inCongestionAvoidance = false, overSsthresh = false,
				timeOut = false;
		int nextInLine = 0, numOfPacketsToSend, prevAckNum = Integer.MAX_VALUE,
				countAck, droppedPacket;
		double prevCwnd;

		/**
		 * starts the rec thread
		 */
		@Override
		public void run() {
			//System.out.println("Receiver thread started");
			rec();
		}

		/**
		 * This method basically handles the received packets and accordingly puts the next packets in the queue
		 * on which it is synchronized
		 */
		public void rec() {
			System.out.println("Waiting for clients");
			TCPHeader packet = null;
			while (true) {
				try {
					if (!firstTime) {
						//System.out.println("Setting Timeout");
						serverSocket.setSoTimeout(10000);
					}
				} catch (SocketException e1) {
					e1.printStackTrace();
				}
				try {
					receivePacket = new DatagramPacket(receiveData,
							receiveData.length);
					try {
						serverSocket.receive(receivePacket);
						IPAddress = receivePacket.getAddress();
						port = receivePacket.getPort();
						receiveData = receivePacket.getData();
						firstTime = false;
						packet = getPacketObject(receiveData);
						System.out.println("CWND" + cwnd);
						System.out.println("Received ack: "
								+ packet.getAckNum());
					} catch (SocketTimeoutException ste) {
						timeOut = true;
					}
					if (timeOut) {
						System.out.println("TimeOut occured");
						restarting(prevAckNum);
					} else if (packet.getAckNum() == prevAckNum) {
						countAck++;
						if (countAck > 3) {
							continue;
						}
						System.out.println(countAck + " dupacks");
						if (countAck == 3) {
							System.out.println("Packet loss occured: "
									+ (packet.getAckNum()));
							restarting(packet.getAckNum());
						} else {
							restart = false;
							continue;
						}
					}
					if (inCongestionAvoidance) {
						System.out.println("In Congestion Avoidance");
						if (restart) {
							numOfPacketsToSend = 1;
							// send the lost packet
							packetToSend = droppedPacket;
						} else if (cwnd >= ssthresh || overSsthresh) {
							overSsthresh = true;
							if ((int) cwnd - (int) prevCwnd >= 1) {
								prevCwnd = cwnd;
								cwnd += (1 / cwnd);
								numOfPacketsToSend = 2;
							} else {
								prevCwnd = cwnd;
								cwnd += (1 / cwnd);
								numOfPacketsToSend = 1;
							}
							packetToSend = nextInLine;
						} else {
							if (cwnd == 1) {
								numOfPacketsToSend = 1;
							} else {
								numOfPacketsToSend = 2;
							}
							prevCwnd = cwnd;
							cwnd++;
							packetToSend = nextInLine;
						}
					} else {
						if (packet.getSynFlag()) {
							numOfPacketsToSend = 1;
							nextInLine = 1;
							packetToSend = nextInLine;
							new Sender().start();
						} else {
							numOfPacketsToSend = 2;
							packetToSend = nextInLine;
						}
						cwnd++;
					}
					if (packetToSend + numOfPacketsToSend <= totalPackets + 1) {
						synchronized (packetsInLine) {
							for (int count = 0; count < numOfPacketsToSend; count++) {
								packetsInLine.add(packets.get(packetToSend));
								/*System.out.println(packets.get(packetToSend)
										.getSeqNum() + " put in the queue; ");*/
								if (!restart) {
									nextInLine++;
								}
								packetToSend++;
							}
							packetsInLine.notifyAll();
						}
					}
					prevAckNum = packet.getAckNum();
					if (restart) {
						System.out.println("Inside restart increase cwnd");
						cwnd++;
						restart = false;
					}
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		/**
		 * restarts the system in case of timeout or packet loss
		 * @param packetLoss the lost packet
		 */
		public void restarting(int packetLoss) {
			inCongestionAvoidance = true;
			droppedPacket = packetLoss;
			timeOut = false;
			ssthresh = (int) cwnd / 2;
			restart = true;
			cwnd=0;
		}
	}

	/**
	 * 
	 * @author Shikha Soni
	 *
	 */
	class Sender extends Thread {
		/**
		 * start the sender thread
		 */
		@Override
		public void run() {
			while (true) {
				synchronized (packetsInLine) {
					if (packetsInLine.size() == 0) {
						try {
							packetsInLine.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					for (int index = 0; index < packetsInLine.size(); index++) {

						System.out.println(packetsInLine.peek().getSeqNum()
								+ " next sending");
						TCPHeader header = packetsInLine.poll();
						sendData = getPacketBytes(header);
						// System.out.println(nextInLine + " sent");
						sendPacket = new DatagramPacket(sendData,
								sendData.length, IPAddress, port);
						try {
							serverSocket.send(sendPacket);
							Thread.sleep(1000);
						} catch (IOException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}