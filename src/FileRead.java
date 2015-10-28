import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;

public class FileRead {
	String fileName;
	BufferedReader reader;
	int num_Of_packets=10;
	
	public FileRead(String fileName){
		this.fileName=fileName;
		try {
			reader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void setnum_Of_packets(int packets){
		num_Of_packets=packets;
	}

	public String readNext() {
		String parts = null;
		return parts;
	}
}
