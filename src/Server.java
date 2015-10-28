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

//import java.util.Random;

public class Server extends Thread {

	private DatagramSocket serverSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private HashMap<Integer, TCPHeader> packets;
	int[] ackCount;
	private int /* RTT, */port, ssthresh, numPacketsSent, lastAckRec;
	volatile int cwnd = 1;
	double startTime;
	private int timeOut;
	FileRead files;
	String fileName;
	byte[] sendData = new byte[1024];
	byte[] receiveData = new byte[1024];
	InetAddress IPAddress;

	public Server() {
		try {
			// Random random= new Random();
			serverSocket = new DatagramSocket(port);
			// timeOut=random.nextInt(20);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {
		makePackets();
		rec();
	}

	public void rec() {
		TCPHeader packet;
		while (true) {
			try {
				serverSocket = new DatagramSocket(8999);
				receivePacket = new DatagramPacket(receiveData,
						receiveData.length);
				serverSocket.receive(receivePacket);
				byte[] data = receivePacket.getData();
				System.out.println(data.length);
				ByteArrayInputStream b = new ByteArrayInputStream(data);
				ObjectInputStream o = new ObjectInputStream(b);
				packet = (TCPHeader) o.readObject();
				if(packet.getSynFlag()){
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							System.out.println("New sending thread made");
							send();
						}
					});
					t.start();
				}
				else if(packet.getAckFlag()){
					
				}
			} catch (SocketException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	public void send() {
		while (numPacketsSent == packets.size()) {
			for (int packet_num = 1; packet_num < 10; packet_num++) {
				ByteArrayOutputStream b = new ByteArrayOutputStream();
				try {
					ObjectOutputStream o = new ObjectOutputStream(b);
					o.writeObject(packets.get(packet_num));
					sendData = b.toByteArray();
					sendPacket = new DatagramPacket(sendData, sendData.length,
							IPAddress, 8999);
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	public void cwndInc() {
		new TimerThread(timeOut).start();
		cwnd++;
	}

	public void makePackets() {
		TCPHeader header;
		files = new FileRead(fileName);
		for (int packet_num = 1; packet_num < 10; packet_num++) {
			header = new TCPHeader();
			header.setSeqNum(packet_num);
			header.setData(files.readNext());
			// header.setCheckSum();
			packets.put(packet_num, header);
		}
		send();
	}

	public static void main(String[] args) {
		// start the receiver, then start the sender on separate thread
		new Server().start();
	}
}

class TimerThread extends Thread {
	long startTime;
	long endTime;
	int timeOut = 0;

	public TimerThread(int timeOut) {
		// stop the previous thread, start a new one
		this.timeOut = timeOut;
	}

	public void run() {
		// timer up till the time out period
		try {
			Thread.sleep(timeOut);
			// after waking up send a trigger for timeOut
			// call the resend method with the packet number
			// get the packet number missing. this case occurs only when the
			// number of packets sent is less than 3, or the number of packets
			// left to be acknowledged is less than 3
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}