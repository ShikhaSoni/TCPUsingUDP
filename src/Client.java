import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.TreeMap;

public class Client extends Thread {
	private DatagramSocket clientSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private TreeMap<Integer, TCPHeader> packets;
	InetAddress IPAddress;
	byte[] sendData = new byte[1024];
	byte[] receiveData = new byte[1024];
	int sendflag, droppedSeqNum, prevSeqNum=0;
	boolean DroppedFlag = false;

	public Client() {
		try {
			packets = new TreeMap<Integer, TCPHeader>();
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
			//System.out.println("Packet" + recAckNum + " sending");
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
		int recSeqNum = 0;
		while (true) {
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				clientSocket.receive(receivePacket);
				receiveData = receivePacket.getData();
				packet = getPacketObject(receiveData);
				recSeqNum = packet.getSeqNum();
				packets.put(packet.getSeqNum(), packet);
				System.out.println("Packet " + recSeqNum + " received");
				// on receiving insert it to treemap
				send(nextSeqNum(recSeqNum));
				if (packet.lastBit()) {
					System.out.println(packet.getSeqNum()
							+ " has been received hence opening");
					openPacket();
				}
				// else
				// send(droppedAckNum);
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
	
	public int nextSeqNum(int latestSeqNum){
		
		if(latestSeqNum-prevSeqNum>1){
		}
		return latestSeqNum;
	}

	public void openPacket() {
		File newFile = new File("output.txt");
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(newFile, true);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		for (int index = 1; index < packets.size(); index++) {
			// extracting the data and writing to the file.
			try {
				System.out.println("Writing to the file");
				fos.write(packets.get(index).getData());
				System.out.println(new String(packets.get(index).getData()));
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void main(String[] args) {
		new Client().send(0);
	}

}
