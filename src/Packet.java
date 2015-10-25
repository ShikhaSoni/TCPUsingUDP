/**
 * 
 * @author Shikha Soni
 *
 *         This class represents the packet/TCP segment structure It stores bits
 *         to represent if the packet is a sync message, acknowledgement or
 *         information message.
 */
public class Packet {
	byte[] flag = new byte[9];
	byte[] seqNum = new byte[32];
	byte[] ackNum = new byte[32];
	byte[] checksum = new byte[16];

	public void setFlag() {

	}

	public void setSeqNum() {

	}

	public void setAckNum() {

	}

	public void setCheckSum() {

	}

	public void getFlag() {

	}

	public void getSeqNum() {

	}

	public void getAckNum() {

	}

	public void getCheckSum() {

	}
}
