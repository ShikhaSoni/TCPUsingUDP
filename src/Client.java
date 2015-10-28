import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.TreeMap;


public class Client {
	private DatagramSocket serverSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private TreeMap<Integer, TCPHeader> packets;
	int nextExpected;

	public TCPHeader makeAck(int ackNum){
		TCPHeader header= null;
		
		return header;
	}
	
	public void send(){
		//first message is syn message
	}
	public void rec(){
		
	}
	public static void main(String[] args) {

	}

}
