import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {

	private DatagramSocket serverSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private HashMap<Integer, TCPHeader> packets;
	private HashMap<Integer, TCPHeader> ack;
	private int RTT, port = 8999, cwnd = 1, ssthresh, timeOut, numPackets;
	FileRead files;
	String fileName;

	public Server() {
		try {
			serverSocket = new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void run() {

	}

	public void rec() {

	}

	public void send() {

	}

	public void makePackets() {
		TCPHeader header;
		files = new FileRead(fileName);
		for (int packet_num = 0; packet_num < 10; packet_num++) {
			if (packet_num == 0) {
				header = new TCPHeader();
				header.setSynFlag(true);
				packets.put(packet_num, header);
				break;
			}
			header = new TCPHeader();
			header.setSeqNum(packet_num);
			header.setData(files.readNext());
			// header.setCheckSum();
			packets.put(packet_num, header);
		}
	}

	public static void main(String[] args) {
		// start the receiver, then start the sender on separate thread
	}

}
