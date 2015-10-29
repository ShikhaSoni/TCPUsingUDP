import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class FileRead {
	String fileName;
	FileInputStream fileInputStream = null;
	HashMap<Integer, byte[]> parts = new HashMap<Integer, byte[]>();
	int num_Of_packets = 10;
	File file;
	byte[] bFile;
	byte[] small;
	int totalPackets;

	public FileRead(String fileName) {
		this.fileName = fileName;
		file = new File(fileName);
		bFile = new byte[(int) file.length()];
		small = new byte[512];
		try {
			fileInputStream = new FileInputStream(file);
			fileInputStream.read(bFile);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getNumOfPackets() {
		return num_Of_packets;
	}

	public void readNext() {
		// fill up the hashmap
		int count = 0, index = 0;
		while (count < bFile.length) {
			index++;
			try {
				fileInputStream.read(small);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			parts.put(index, small);
			count += 512;
		}
		byte[] remaining= new byte[bFile.length-(count-512)];
		try {
			fileInputStream.read(remaining);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		parts.put(index++,remaining);
		totalPackets=parts.size();
		System.out.println("Completed making parts");
	}
}
