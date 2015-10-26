
public class TCPHeader {
	private boolean flag;
	private int seqNum, ackNum;
	private byte[] checkSum;
	
	public void setFlag(boolean flag) {
		this.flag = flag;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	public void setAckNum(int ackNum) {
		this.ackNum = ackNum;
	}

	public void setCheckSum(byte[] checkSum) {
		this.checkSum = checkSum;
	}

	public boolean getFlag() {
		return flag;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public int getAckNum() {
		return ackNum;
	}

	public byte[] getCheckSum() {
		return checkSum;
	}
}
