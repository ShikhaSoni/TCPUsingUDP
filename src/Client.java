import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TreeMap;

//import java.util.TreeMap;

public class Client extends Thread {
	private DatagramSocket clientSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private TreeMap<Integer, TCPHeader> packets;
	InetAddress IPAddress;
	byte[] sendData = new byte[1024];
	byte[] receiveData = new byte[1024];
	int sendflag, droppedAckNum;
	boolean pDroppedFlag=false;

	public Client() {
		try {
			packets= new TreeMap<Integer, TCPHeader>();
			clientSocket = new DatagramSocket(8999);
			IPAddress = InetAddress.getByName("localhost");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TCPHeader makeAck(int ackNum) {
		TCPHeader header = null;

		return header;
	}

	public void send(int recAckNum) {
		TCPHeader packet = new TCPHeader();
		if (sendflag == 0) {
			packet.setSynFlag(true);
			sendflag++;
			System.out.println("Sending sync message");
		} else {
			packet.setAckFlag(true);
			packet.setAckNum(recAckNum);
			System.out.println("Ack: " + recAckNum);
		}
		try {
			sendData = getPacketBytes(packet);
			sendPacket = new DatagramPacket(sendData, sendData.length,
					IPAddress, 7999);
			System.out.println("Packet" + recAckNum + " sending");
			clientSocket.send(sendPacket);
			if (packet.getSynFlag()) {
				Thread t = new Thread(new Runnable() {
					@Override
					public void run() {
						rec();
					}
				});
				t.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void rec() {
		TCPHeader packet;
		int recAckNum = 0;
		while (true) {
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				clientSocket.receive(receivePacket);
				receiveData = receivePacket.getData();
				packet = getPacketObject(receiveData);
				recAckNum = packet.getSeqNum();
				System.out
						.println("Packet " + recAckNum + " received");
				//on receiving insert it to treemap
				packets.put(packet.getSeqNum(), packet);
				if(recAckNum==3 || recAckNum==6){
					pDroppedFlag=true;
					droppedAckNum=recAckNum;
					continue;
					//dropping the packet
				}
				if(!pDroppedFlag)
					send(recAckNum+1);
				else
					send(droppedAckNum);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public byte[] getPacketBytes(TCPHeader packet) {
		ByteArrayOutputStream b = new ByteArrayOutputStream();
		try {
			ObjectOutputStream o = new ObjectOutputStream(b);
			o.writeObject(packet);
			o.flush();
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
	public void openPacket(){
		for(int index=0; index<packets.size(); index++){
			//extracting the data and writing to the file.
			
		}
	}

	public static void main(String[] args) {
		new Client().send(0);
	}

}
