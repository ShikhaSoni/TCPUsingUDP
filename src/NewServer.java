import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
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
	int port, totalPackets, cwnd, ssthresh, packetToSend;
	

	public NewServer() {
		file = new FileRead("words.txt");
		try {
			serverSocket = new DatagramSocket(7999);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void startSystem() {
		Receiver receiver = new Receiver();
		receiver.start();
	}

	public static void main(String[] args) {
		NewServer server = new NewServer();
		server.makePackets();
		server.startSystem();
	}

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

	public void makePackets() {
		HashMap<Integer, byte[]> parts = file.readNext();
		this.totalPackets = file.totalPackets;
		System.out.println(totalPackets);
		TCPHeader header;
		for (int packet_num = 1; packet_num <= totalPackets; packet_num++) {
			header = new TCPHeader();
			header.setSeqNum(packet_num);
			header.setData(parts.get(packet_num));
			// header.setCheckSum();
			if (packet_num == totalPackets) {
				header.setLastBit(true);
			}
			packets.put(packet_num, header);
		}
	}

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

	class Receiver extends Thread {
		boolean firstTime = true, restart = false;
		int nextInLine = 0, numOfPacketsToSend, prevAckNum = Integer.MAX_VALUE,
				countAck, droppedPacket;

		public void run() {
			System.out.println("Receiver thread started");
			rec();
		}

		public void rec() {
			System.out.println("Waiting for clients");
			TCPHeader packet;
			while (true) {
				try {
					receivePacket = new DatagramPacket(receiveData,
							receiveData.length);
					serverSocket.receive(receivePacket);
					IPAddress = receivePacket.getAddress();
					port = receivePacket.getPort();
					receiveData = receivePacket.getData();
					packet = getPacketObject(receiveData);
					System.out.println("Received ack: "+packet.getAckNum());
					if (packet.getAckNum() == prevAckNum) {
						countAck++;
						if(countAck>3){
							continue;
						}
						System.out.println(countAck + " dupacks");
						if (countAck == 3) {
							System.out.println("Ack loss occured: "
									+ (packet.getAckNum()));
							// retransmit the lost packet
							restart = true;
							cwnd = 0;
							droppedPacket=packet.getAckNum();
							ssthresh=cwnd/2;
							// start sending and wait till ssthresh,
							// retransmit the lost and then increment
						} else {
							restart = false;
							continue;
						}
					}
					cwnd++;
					if (packet.getSynFlag()) {
						numOfPacketsToSend = 1;
						nextInLine=1;
						packetToSend=nextInLine;
						new Sender().start();
					} else if (restart) {
						numOfPacketsToSend = 1;
						packetToSend=droppedPacket;
					} else{
						numOfPacketsToSend = 2;
						packetToSend=nextInLine;
					}
					System.out.println("CWND"+ cwnd);
					if (packet.getAckNum() < totalPackets + 1) {
						synchronized (packetsInLine) {
							for (int count = 0; count < numOfPacketsToSend; count++) {
								packetsInLine.add(packets.get(packetToSend));
								System.out.println(packets.get(packetToSend).getSeqNum()+ " put in the queue; ");
								if(!restart){
									nextInLine++;
									//System.out.println("Increment "+nextInLine);
								}
								packetToSend++;
							}
							packetsInLine.notifyAll();
						}
					}
					prevAckNum = packet.getAckNum();
					restart=false;
				} catch (SocketException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class Sender extends Thread {
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
						
						System.out.println(packetsInLine.peek().getSeqNum()+" next sending");
						sendData = getPacketBytes(packetsInLine.poll());
						//System.out.println(nextInLine + " sent");
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