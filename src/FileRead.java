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

	public FileRead(String fileName) {
		
		this.fileName = fileName;
		file = new File(fileName);
		System.out.println(file.length());
		try {
			fileInputStream = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} 
	}

	public int getNumOfPackets() {
		return totalPackets;
	}
	public void print(HashMap<Integer, byte[]> parts){
		for(int index=1; index<parts.size(); index++){
			System.out.println("Part "+ index +":");
			System.out.println(new String(parts.get(index)));
			System.out.println("--------------------------"+ parts.get(index).length);
		}
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			parts.put(index, small);
			count += 512;
		}
		byte[] remaining= new byte[(int)file.length()-(count-512)];
		if(remaining.length>0){
			//System.out.println("remaining"+ remaining.length);
			try {
				fileInputStream.read(remaining);
				//System.out.println(new String(remaining));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			parts.put(index+1,remaining);
			//System.out.println(new String(parts.get(index+1)));
		}
		totalPackets=parts.size();
	//	print(parts);
		//System.out.println("Completed making "+totalPackets +" parts");
		return parts;
	}
}
