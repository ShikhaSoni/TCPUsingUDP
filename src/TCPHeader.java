import java.io.Serializable;


public class TCPHeader implements Serializable{
	private static final long serialVersionUID = 1L;
	private boolean ack_flag=false;
	private boolean syn_flag=false;
	private boolean data_flag=false;
	private int seqNum, ackNum;
	private byte[] checkSum=null;
	private byte[] data=null;
	private boolean lastBit=false;
	
	public void setAckFlag(boolean ack_flag) {
		this.ack_flag = ack_flag;
	}
	public void setSynFlag(boolean syn_flag){
		this.syn_flag=syn_flag;
	}

	public void setDataFlag(boolean data_flag){
		this.data_flag=data_flag;
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
	
	public void setData(byte[] data){
		this.data=data;
	}
	
	public void setLastBit(boolean lastBit){
		this.lastBit=lastBit;
	}

	public boolean getAckFlag() {
		return ack_flag;
	}
	public boolean getSynFlag(){
		return syn_flag;
	}
	
	public boolean getDataFlag(){
		return data_flag;
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
	public byte[] getData(){
		return data;
	}
	
	public boolean lastBit(){
		return lastBit;
	}
}
