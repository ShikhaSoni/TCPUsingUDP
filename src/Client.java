/**
 * @author Shikha Soni
 */
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
import java.util.ArrayList;
import java.util.TreeMap;


public class Client extends Thread {
	private DatagramSocket clientSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private TreeMap<Integer, TCPHeader> packets;
	ArrayList<Integer> misSeqNum;
	InetAddress IPAddress;
	byte[] sendData = new byte[1024];
	byte[] receiveData = new byte[1024];
	int sendflag, port;

	/**
	 * Constructor takes te IP address and the port number of the server
	 * @param IPAddress
	 * @param port
	 */
	public Client(String IPAddress, int port) {
		try {
			packets = new TreeMap<Integer, TCPHeader>();
			clientSocket = new DatagramSocket(8999);
			this.IPAddress = InetAddress.getByName(IPAddress);//InetAddress.getByName("localhost");
			this.port=port;
			misSeqNum= new ArrayList<Integer>();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * send method takes the currently rec ack number 
	 * @param recAckNum
	 */
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
					IPAddress, port);
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
	/**
	 * Return the checksum of the packet
	 * @param object
	 * @return
	 */
	public String getCheckSum(TCPHeader object){
		byte[] a=object.getData();
		String checkSum="";
		for(int i=0;i<2;i++){
			Byte b=a[i];
			checkSum+=b.intValue();
		}
		return checkSum;
	}
	public boolean containsPacket(int seqNum){
		if(packets.containsKey(seqNum)){
			return true;
		}
		return false;
	}

	/**
	 * rec thread to rec the data packets
	 */
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
				System.out.println("Packet " + recSeqNum + " received");
				String s=packet.getCheckSum();
				if(!s.equals(getCheckSum(packet))){
					System.out.println("CheckSum did not match");
					continue;
				}
				if(packet.getSeqNum()%10==0 && !containsPacket(packet.getSeqNum())){
					System.out.println("Dropped packet"+packet.getSeqNum());
					continue;
				}
				send(nextSeqNum(recSeqNum));
				packets.put(packet.getSeqNum(), packet);
				if (packet.lastBit() && packet.getSeqNum()==packets.size()) {
					System.out.println(packet.getSeqNum()
							+ " has been received hence opening");
					openPacket();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 
	 * @param packet
	 * @return
	 */
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

	/**
	 * This fucntion returns an object from the byte format
	 * @param packet The byte array of the serialized packet
	 * @return The actual TCPHeader object extracted from the byte array 
	 */
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
	/**
	 * This method predicts the ext data packet that is expected  in line
	 * @param latestSeqNum
	 * @return
	 */
	public int nextSeqNum(int latestSeqNum){
		//expected next in line
		boolean flag= false;
		if(misSeqNum.isEmpty()){
			misSeqNum.add(latestSeqNum+1);
			//System.out.println(latestSeqNum+1+" added---1");
		}
		else{
			//remove the received packet
			for(int index=0; index<misSeqNum.size(); index++){
				if(misSeqNum.get(index)==latestSeqNum){
					System.out.println(misSeqNum.get(index)+" removed");
					if(misSeqNum.size()>1){
						flag=true;
					}
					misSeqNum.remove(index);
				}
			}
			if(!flag){
			misSeqNum.add(latestSeqNum+1);
			//System.out.println(misSeqNum.get(misSeqNum.size()-1)+" added---2");
			flag=false;
			}
			return misSeqNum.get(0);
		}
		return latestSeqNum+1;
	}

	/**
	 * After all the packets are received this mthod decodes the bytes and writes it in a new file
	 */
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
				fos.write(packets.get(index).getData());
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Completed writing to the file");
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Client(args[0], Integer.parseInt(args[1])).send(0);
	}

}
