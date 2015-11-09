/**
 * @author Shikha Soni
 * This class helps read the data into small byte chunks
 */
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class FileRead {
	String fileName;
	FileInputStream fileInputStream = null;
	File file;
	int totalPackets;

	/**
	 * 
	 * @param fileName the file to be converted
	 */
	public FileRead(String fileName) {
		
		this.fileName = fileName;
		file = new File(fileName);
		//System.out.println(file.length());
		try {
			fileInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}

	public int getNumOfPackets() {
		return totalPackets;
	}
	
	public HashMap<Integer, byte[]> readNext() {
		HashMap<Integer, byte[]> parts = new HashMap<Integer, byte[]>();
		// fill up the hashmap
		int count = 0, index = 0;
		
		while (count < file.length()) {
			byte[] small = new byte[512];
			index++;
			try {
				fileInputStream.read(small);
			} catch (IOException e) {
				e.printStackTrace();
			}
			parts.put(index, small);
			count += 512;
		}
		byte[] remaining= new byte[(int)file.length()-(count-512)];
		if(remaining.length>0){
			try {
				fileInputStream.read(remaining);
			} catch (IOException e) {
				e.printStackTrace();
			}
			parts.put(index+1,remaining);
		}
		totalPackets=parts.size();
		return parts;
	}
}
