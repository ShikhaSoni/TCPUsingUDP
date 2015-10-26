import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

public class FileRead {
	String fileName;
	BufferedReader reader;
	int num_Of_packets;
	ArrayList<String> parts = new ArrayList();
	
	public FileRead(String fileName){
		this.fileName=fileName;
		try {
			reader = new BufferedReader(new FileReader(fileName));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void setnum_Of_packets(int packets){
		num_Of_packets=packets;
	}

	public ArrayList<String> read() {
		
		return parts;
	}
}
