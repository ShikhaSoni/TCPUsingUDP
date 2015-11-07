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
	int port, totalPackets, ssthresh, packetToSend;
	double cwnd=1.0;
	

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
		boolean firstTime = true, restart = false, inCongestionAvoidance=false, overSsthresh=false;
		int nextInLine = 0, numOfPacketsToSend, prevAckNum = Integer.MAX_VALUE,
				countAck, droppedPacket;
		double prevCwnd;

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
					//reset timer
					//if timer ends send the last ack rec
					
					IPAddress = receivePacket.getAddress();
					port = receivePacket.getPort();
					receiveData = receivePacket.getData();
					packet = getPacketObject(receiveData);
					System.out.println("CWND"+ cwnd);
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
							//retransmit lost packet
							droppedPacket=packet.getAckNum();
							ssthresh=(int)cwnd/2;
							cwnd = 0;
							inCongestionAvoidance=true;
							// start sending and wait till ssthresh,
							// retransmit the lost and then increment
						} else {
							restart = false;
							continue;
						}
					}
					if(inCongestionAvoidance){
						System.out.println("In Congestion Avoidance");
						if (restart) {
							System.out.println("In retransmission with cwnd "+cwnd);
							numOfPacketsToSend = 1;
							//send the lost packet
							packetToSend=droppedPacket;
						}
						else if(cwnd>=ssthresh || overSsthresh){
							overSsthresh=true;
							//System.out.println(prevCwnd);
							if((int)cwnd-(int)prevCwnd>=1){
								//System.out.println("----------2 packets "+(cwnd-prevCwnd));
								prevCwnd=cwnd;
								cwnd+=(1/cwnd);
								numOfPacketsToSend=2;
							}
							else {
								//System.out.println("-----------1"+(cwnd-prevCwnd));
								prevCwnd=cwnd;
								cwnd+=(1/cwnd);
								numOfPacketsToSend=1;
							}
							packetToSend=nextInLine;
						}
						else{
							if(cwnd==1){
								//System.out.println("CWND is one again"+ cwnd);
								numOfPacketsToSend=1;
							}
							else{
								//System.out.println("CWND when cwnd is not 1 again: "+cwnd);
								numOfPacketsToSend=2;
							}
							prevCwnd=cwnd;
							cwnd++;
							packetToSend=nextInLine;
						}
					}
					else{
						if (packet.getSynFlag()) {
							numOfPacketsToSend = 1;
							nextInLine=1;
							packetToSend=nextInLine;
							new Sender().start();
						}  else{
							numOfPacketsToSend = 2;
							packetToSend=nextInLine;
						}
						cwnd++;
					}
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
					if(restart){
						cwnd++;
						restart=false;
					}
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