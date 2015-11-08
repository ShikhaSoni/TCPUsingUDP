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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.xml.bind.DatatypeConverter;

public class Client extends Thread {
	private DatagramSocket clientSocket = null;
	private DatagramPacket receivePacket = null;
	private DatagramPacket sendPacket = null;
	private TreeMap<Integer, TCPHeader> packets;
	ArrayList<Integer> misSeqNum;
	InetAddress IPAddress;
	byte[] sendData = new byte[1024];
	byte[] receiveData = new byte[1024];
	int sendflag;

	public Client() {
		try {
			packets = new TreeMap<Integer, TCPHeader>();
			clientSocket = new DatagramSocket(8999);
			IPAddress = InetAddress.getByName("localhost");
			misSeqNum= new ArrayList<Integer>();
		} catch (Exception e) {
			e.printStackTrace();
		}
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
	
	public String getCheckSum(byte[] object){
		ByteArrayOutputStream baos = null;
	    ObjectOutputStream oos = null;
	    byte[] thedigest = null;
	    try {
	        baos = new ByteArrayOutputStream();
	        oos = new ObjectOutputStream(baos);
	        oos.writeObject(object);
	        MessageDigest md = MessageDigest.getInstance("MD5");
	        thedigest = md.digest(baos.toByteArray());
	    } catch (IOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} 
	    return DatatypeConverter.printHexBinary(thedigest);
	}

	public void rec() {
		TCPHeader packet;
		int recSeqNum = 0;
		boolean flag= true;
		while (true) {
			receivePacket = new DatagramPacket(receiveData, receiveData.length);
			try {
				clientSocket.receive(receivePacket);
				receiveData = receivePacket.getData();
				packet = getPacketObject(receiveData);
				recSeqNum = packet.getSeqNum();
				System.out.println("Packet " + recSeqNum + " received");
				//drop the packets
				//check checksum
				String checkSum=packet.getCheckSum();
				packet.setCheckSum(null);
				if(!checkSum.equals(getCheckSum(receiveData))){
					System.out.println("CheckSum did not match");
					continue;
				}
				if(packet.getSeqNum()==6 && flag){
					flag=false;
					System.out.println("Dropping packet"+packet.getSeqNum());
					continue;
				}
				send(nextSeqNum(recSeqNum));
				packets.put(packet.getSeqNum(), packet);
				if (packet.lastBit()) {
					System.out.println(packet.getSeqNum()
							+ " has been received hence opening");
					openPacket();
				}
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
		//expected next in line
		boolean flag= false;
		if(misSeqNum.isEmpty()){
			misSeqNum.add(latestSeqNum+1);
			System.out.println(latestSeqNum+1+" added---1");
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
			System.out.println(misSeqNum.get(misSeqNum.size()-1)+" added---2");
			flag=false;
			}
			return misSeqNum.get(0);
		}
		return latestSeqNum+1;
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
				fos.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Client().send(0);
	}

}
