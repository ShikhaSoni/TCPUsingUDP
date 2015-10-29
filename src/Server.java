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
import java.util.Random;

public class Server extends Thread {

	private DatagramSocket serverSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private HashMap<Integer, TCPHeader> packets = new HashMap<Integer, TCPHeader>();
	int[] ackCount = new int[10];// stores for the ack packets
	private int port, ssthresh, numPacketsSent = 1, totalPackets=0, timeOut, cwnd = 1;// number of total packets
									// sent till now
	FileRead file;
	// double startTime;
	byte[] sendData = new byte[1024];
	byte[] receiveData = new byte[1024];
	InetAddress IPAddress;

	public Server() {
		file= new FileRead("words.txt");
		file.readNext();
		this.totalPackets=file.totalPackets;
		try {
			Random random = new Random();
			serverSocket = new DatagramSocket(7999);
			timeOut = random.nextInt(20);
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
				System.out.println("Packet seq num received : "
						+ packet.getAckNum());
				cwnd++;
				if (packet.getSynFlag()) {
					Thread t = new Thread(new Runnable() {
						@Override
						public void run() {
							send();
						}
					});
					t.start();
				} else if (packet.getAckFlag()) {
					ackCount[packet.getAckNum()]++;
					if (ackCount[packet.getAckNum()] == 3) {
						// if the ack received is 3 send 3 again and keep a
						// track of how many are sent and send then onwards

						// resend packet and then resume
					}
				}
			} catch (SocketException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public void send() {
		System.out.println("Starting to send the packets");
		while (numPacketsSent != packets.size()) {
			int numSent = 0;// number of packets sent
			while (numSent != cwnd) {
				// send the packet, and wait for ack if cwnd=1
				sendData = getPacketBytes(packets.get(numPacketsSent));
				sendPacket = new DatagramPacket(sendData, sendData.length,
						IPAddress, port);
				try {
					Thread.sleep(1000);
					serverSocket.send(sendPacket);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				numPacketsSent++;
				numSent++;
				// wait till at least one ack is received, increase window size.
				// if this wait reaches TO, cwnd=1, ssthresh=cwnd/2 retransmit
				// from the last
			}
			// if cwnd has increased, then only continue sending, else wait till
			// timeout
			// cwnd increases if ack is received
		}
	}

	public byte[] getPacketBytes(TCPHeader packet) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		try {
			ObjectOutputStream o = new ObjectOutputStream(b);
			o.writeObject(packet);
			o.flush();
			System.out.println("Packet " + packet.getSeqNum() + " sending");
		} catch (IOException e) {
			e.printStackTrace();
		}
		return b.toByteArray();
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
		TCPHeader header;
		for (int packet_num = 1; packet_num < totalPackets; packet_num++) {
			header = new TCPHeader();
			header.setSeqNum(packet_num);
			header.setData(file.parts.get(packet_num));
			// header.setCheckSum();
			packets.put(packet_num, header);
			System.out.println(packet_num + " Packet made");
		}
	}

	public static void main(String[] args) {
		// start the receiver, then start the sender on separate thread
		new Server().start();
	}
}

/*
 * class TimerThread extends Thread { long startTime; long endTime; int timeOut
 * = 0;
 * 
 * public TimerThread(int timeOut) { // stop the previous thread, start a new
 * one this.timeOut = timeOut; }
 * 
 * public void run() { // timer up till the time out period try {
 * Thread.sleep(timeOut); // after waking up send a trigger for timeOut // call
 * the resend method with the packet number // get the packet number missing.
 * this case occurs only when the // number of packets sent is less than 3, or
 * the number of packets // left to be acknowledged is less than 3 } catch
 * (InterruptedException e) { // TODO Auto-generated catch block
 * e.printStackTrace(); } } }
 */

/*
 * System.out.println("Inside while the packets"); for (int packet_num = 1;
 * packet_num < 10; packet_num++) { sendData =
 * getPacketBytes(packets.get(packet_num)); sendPacket = new
 * DatagramPacket(sendData, sendData.length, IPAddress, port); try {
 * Thread.sleep(1000); serverSocket.send(sendPacket); } catch
 * (InterruptedException e) { e.printStackTrace(); } catch (IOException e) {
 * e.printStackTrace(); }
 */