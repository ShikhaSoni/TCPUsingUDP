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

public class Server extends Thread {

	private DatagramSocket serverSocket = null;
	private DatagramPacket receivePacket = null;
	private HashMap<Integer, TCPHeader> packets = new HashMap<Integer, TCPHeader>();
	private FileRead file;
	int cwnd = 0, packetsAlreadySent = 0, totalPackets, port,
			prevAckNum = Integer.MAX_VALUE, countAck, ssthresh, nextInLine;
	byte[] receiveData = new byte[1024];
	InetAddress IPAddress;
	boolean Lostflag=false;

	public Server() {
		file = new FileRead("words.txt");
		try {
			serverSocket = new DatagramSocket(7999);
		} catch (SocketException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		makePackets();
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
				//start a new timer thread and destroy the current one 
				//if time out send the last ack received
				
				
				
				//System.out.println(packet.getAckNum()+" Packet received prev ack: "+ prevAckNum+" NextI"+nextInLine);
				boolean restart = false;
				if (packet.getAckNum() == prevAckNum) {
					countAck++;
					System.out.println(countAck + " dupacks");
					if (countAck == 3) {
						 System.out.println("Ack loss occured: " +
						 (packet.getAckNum()));
						nextInLine = packet.getAckNum();
						//retransmit the lost packet
						Lostflag=true;
						restart = true;
						cwnd = 1;
						ssthresh = cwnd / 2;
						// start sending and wait till ssthresh,
						// retransmit the lost and then increment
					} else {
						restart = false;
						continue;
					}
				} else {
					if (packet.getSynFlag()) {
						nextInLine = 1;
						cwnd = 1;
					} else {
						restart = false;
						//nextInLine=packet.getAckNum();
						if(Lostflag){
							cwnd+=(1/cwnd);
						}
						cwnd++;
						prevAckNum = packet.getAckNum();
					}
				}
				if (packet.getAckNum() < totalPackets + 1) {
					System.out.println("Nextseq: "+nextInLine);
					Send s=new Send(nextInLine, restart, serverSocket, port,
							IPAddress, packets, cwnd);
					s.setName(Integer.toString(cwnd));
					s.start();
					packetsAlreadySent += cwnd;
					if (packet.getSynFlag() && Lostflag){
						nextInLine += 1;
						Lostflag=false;  
					}
					else
						nextInLine += 2;
				} else
					return;
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

	public static void main(String[] args) {
		// start the receiver, then start the sender on separate thread
		new Server().start();
	}
}

class Send extends Thread {
	private DatagramSocket serverSocket = null;
	private DatagramPacket sendPacket = null;
	boolean restart;
	byte[] sendData = new byte[1024];
	InetAddress IPAddress;
	private HashMap<Integer, TCPHeader> packets;
	static Object lock = new Object();
	int NumToSendNext, port, nextInLine;
	static int seqNum = 1;
	int cwnd, prevcwnd;

	public Send(int nextInLine, boolean restart, DatagramSocket serverSocket,
			int port, InetAddress IPAddress,
			HashMap<Integer, TCPHeader> packets, int cwnd) {
		this.nextInLine = nextInLine;
		this.restart = restart;
		this.port = port;
		this.serverSocket = serverSocket;
		this.IPAddress = IPAddress;
		this.packets = packets;
		this.cwnd = cwnd;

		if (restart || cwnd == 1) {
			NumToSendNext = 1;
			seqNum = 1;
		} else {
			NumToSendNext = 2;
		}
	}

	public void run() {
		send();
	}

	public void send() {
		int numAlreadySent = 0;
		synchronized (lock) {
			if(restart){
				seqNum=1;//change this only after the previous thread is done
				
				
				
				//wait till all the threads before this have been executed
			}
			System.out.println("entered sync block: "+seqNum+" cwnd: "+ cwnd+" NextInLine" +nextInLine);
			while (cwnd != seqNum) {
				lock.notifyAll();
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			prevcwnd=cwnd;
			while (numAlreadySent < NumToSendNext) {
				sendData = getPacketBytes(packets.get(nextInLine));
				System.out.println(nextInLine + " sent");
				sendPacket = new DatagramPacket(sendData, sendData.length,
						IPAddress, port);
				try {
					serverSocket.send(sendPacket);
					Thread.sleep(1000);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				nextInLine++;
				numAlreadySent++;
			}
			if (!restart) {
				seqNum++;
			}
			System.out.println("---------------------------");
		}
		// start timer
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
}

class TimerThread extends Thread {
	long startTime;
	long endTime;
	int timeOut = 0;

	public TimerThread(int timeOut) { // stop the previous thread, start a
		// newone
		this.timeOut = timeOut;
	}

	public void run() { // timer up till the time out period
		try {
			Thread.sleep(timeOut); // after waking up send a trigger for timeOut
			// call the resend method with the packet number
			// get the packet number missing.this case occurs only when the
			// number of packets sent is less than 3, or the number of packets
			// // left to be acknowledged is less than 3
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
