/**
 * 
 * @author Shikha Soni
 *         This class represents the packet/TCP segment structure It stores bits
 *         to represent if the packet is a sync message, acknowledgement or
 *         information message.
 */

public class Packet {
	byte[] flag = new byte[2];
	byte[] seqNum = new byte[4];
	byte[] ackNum = new byte[4];
	byte[] checksum = new byte[2];

	public void setFlag(byte[] flag) {
		this.flag = flag;
	}

	public void setSeqNum(byte[] seqNum) {
		this.seqNum = seqNum;
	}

	public void setAckNum(byte[] ackNum) {
		this.ackNum = ackNum;
	}

	public void setCheckSum(byte[] checkSum) {
		this.checksum = checkSum;
	}

	public byte[] getFlag() {
		return flag;
	}

	public byte[] getSeqNum() {
		return seqNum;
	}

	public byte[] getAckNum() {
		return ackNum;
	}

	public byte[] getCheckSum() {
		return checksum;
	}
}
