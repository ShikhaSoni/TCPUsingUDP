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
	FileRead file;
	int cwnd = 0, packetsAlreadySent = 0, totalPackets, port,
			prevAckNum = Integer.MAX_VALUE, countAck, ssthresh, nextInLine;
	byte[] receiveData = new byte[1024];
	InetAddress IPAddress;

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
				boolean restart = false;
				System.out.println(packet.getAckNum() + " ack");

				if (packet.getAckNum() == prevAckNum) {
					countAck++;
					if (countAck == 3) {
						// resend
						System.out.println("Ack loss occured: "
								+ (packet.getAckNum() - 1));
						nextInLine = packet.getAckNum();
						restart = true;
						cwnd = 1;
						ssthresh = cwnd / 2;
						// start sending and wait till ssthresh,
						//retransmit the lost and then increment
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
						cwnd++;
						prevAckNum = packet.getAckNum();
					}
				}
				if (packet.getAckNum() < totalPackets + 1) {
					//System.out.println(nextInLine+ " will be sent");
					new Send(nextInLine, restart, serverSocket, port,
							IPAddress, packets, totalPackets, cwnd).start();
					packetsAlreadySent += cwnd;
					if(packet.getSynFlag())
						nextInLine += 1;
					else
						nextInLine+=2;
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
		HashMap<Integer, byte[]> parts=file.readNext();
		this.totalPackets = file.totalPackets;
		TCPHeader header;
		for (int packet_num = 1; packet_num <= totalPackets; packet_num++) {
			header = new TCPHeader();
			header.setSeqNum(packet_num);
			header.setData(parts.get(packet_num));
			System.out.println("Part " + packet_num + ": ");
			System.out.println(new String(parts.get(packet_num)));
			//header.setCheckSum();
			if (packet_num == totalPackets) {
				header.setLastBit(true);
			}
			packets.put(packet_num, header);
			System.out.println("------------------------------------");
			//System.out.println();
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
	boolean retsart;
	byte[] sendData = new byte[1024];
	InetAddress IPAddress;
	private HashMap<Integer, TCPHeader> packets;
	static Object lock = new Object();
	int NumToSendNext, port, cwnd, totalPackets, nextInLine;
	static int seqNum=1;
	// static volatile int nextInLine;

	boolean restartFlag = false;

	public Send(int nextInLine, boolean restart, DatagramSocket serverSocket,
			int port, InetAddress IPAddress,
			HashMap<Integer, TCPHeader> packets, int totalPackets, int cwnd) {
		this.nextInLine = nextInLine;
		this.retsart = restart;
		this.port = port;
		this.serverSocket = serverSocket;
		this.IPAddress = IPAddress;
		this.packets = packets;
		this.totalPackets = totalPackets;
		this.cwnd = cwnd;

		if (restart || cwnd == 1) {
			NumToSendNext = 1;
		} else {
			NumToSendNext = 2;
		}
	}

	public void run() {
		send();
	}

	public void send() {
		//cwnd in order
		int numAlreadySent = 0;
		synchronized (lock) {
			System.out.println("CWND: "+cwnd+" SeqNum "+seqNum);
			while(cwnd!=seqNum){
				lock.notifyAll();
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			while (numAlreadySent < NumToSendNext
					&& nextInLine < (totalPackets + 1)) {
				sendData = getPacketBytes(packets.get(nextInLine));
				System.out.print(nextInLine+" ; ");
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
			System.out.println(seqNum);
			seqNum++;
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
