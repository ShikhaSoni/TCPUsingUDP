/**
 * @author Shikha Soni
 * This is the class that stores the structure of the packet used in udp
 */
import java.io.Serializable;


public class TCPHeader implements Serializable{
	private static final long serialVersionUID = 1L;
	private boolean ack_flag=false;
	private boolean syn_flag=false;
	private boolean data_flag=false;
	private int seqNum, ackNum;
	private String checkSum=null;
	private byte[] data=null;
	private boolean lastBit=false;
	
	/**
	 * Setter method for the ack flag
	 * @param ack_flag
	 */
	public void setAckFlag(boolean ack_flag) {
		this.ack_flag = ack_flag;
	}
	/**
	 * Setter method for the syn flag
	 * @param syn_flag
	 */
	public void setSynFlag(boolean syn_flag){
		this.syn_flag=syn_flag;
	}

	/**
	 * Setter method for the data flag
	 * @param data_flag
	 */
	public void setDataFlag(boolean data_flag){
		this.data_flag=data_flag;
	}
	/**
	 * Setter method for the seq num
	 * @param seqNum
	 */
	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}
	/**
	 * Setter method for the ack num
	 * @param ackNum
	 */
	public void setAckNum(int ackNum) {
		this.ackNum = ackNum;
	}

	/**
	 * Setter method for the checksum
	 * @param checkSum
	 */
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
	/**
	 * Setter method for the data
	 * @param data
	 */
	public void setData(byte[] data){
		this.data=data;
	}
	/**
	 * Setter method to indicate the last packet
	 * @param lastBit
	 */
	public void setLastBit(boolean lastBit){
		this.lastBit=lastBit;
	}

	/**
	 * Getter method for the Ack flag
	 * @return
	 */
	public boolean getAckFlag() {
		return ack_flag;
	}
	/**
	 * Getter method for the sync flag
	 * @return
	 */
	public boolean getSynFlag(){
		return syn_flag;
	}
	/**
	 * Getter method for the data flag
	 * @return
	 */
	public boolean getDataFlag(){
		return data_flag;
	}

	/**
	 * Getter method for the Sync num
	 * @return
	 */
	public int getSeqNum() {
		return seqNum;
	}

	/**
	 * Getter method for the Ack num
	 * @return
	 */
	public int getAckNum() {
		return ackNum;
	}

	/**
	 * Getter method for the checksum
	 * @return
	 */
	public String getCheckSum() {
		return checkSum;
	}
	/**
	 * Getter method for the data
	 * @return
	 */
	public byte[] getData(){
		return data;
	}
	/**
	 * Getter method for the lastbit
	 * @return
	 */
	public boolean lastBit(){
		return lastBit;
	}
}
