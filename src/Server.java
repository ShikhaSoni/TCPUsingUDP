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
	private int RTT, port=8999, cwnd=1, ssthresh, timeOut, numPackets;
	
	public Server(){
		try {
			serverSocket= new DatagramSocket(port);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//calculate after every packet received
	public void calcRTT(){
		
	}
	public void run(){
		
	}
	public void rec(){
		
	}
	public void send(){
		
	}
	public void makePackets(){
		TCPHeader header;
		for(int packet_num=1; packet_num<10;packet_num++){
			header= new TCPHeader();
			header.setFlag(false);
			header.setSeqNum(packet_num);
			//header.setCheckSum();
		}
	}
	public static void main(String[] args) {
		//start the receiver, then start the sender on separate thread
	}

}
