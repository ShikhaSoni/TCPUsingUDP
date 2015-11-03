import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

public class FileRead {
	String fileName;
	FileInputStream fileInputStream = null;
	File file;
	byte[] bFile;
	byte[] small;
	int totalPackets;

	public FileRead(String fileName) {
		
		this.fileName = fileName;
		file = new File(fileName);
		System.out.println(file.length());
		small = new byte[512];
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
			index++;
			try {
				fileInputStream.read(small);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			parts.put(index, small);
			System.out.println("Part "+ index +":");
			System.out.println(new String(parts.get(index)));
			System.out.println("--------------------------"+ parts.get(index).length+" Count"+count);
			count += 512;
		}
		byte[] remaining= new byte[(int)file.length()-(count-512)];
		if(remaining.length>0){
			System.out.println("remaining"+ remaining.length);
			try {
				fileInputStream.read(remaining);
				System.out.println(new String(remaining));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			parts.put(index+1,remaining);
			//System.out.println(new String(parts.get(index+1)));
		}
		totalPackets=parts.size();
		//System.out.println("Completed making "+totalPackets +" parts");
		return parts;
	}
}
