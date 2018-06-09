import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Map;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;

public class MetadataClass implements Runnable, Serializable {
	String path;
	static int optionRecieved;
	File f;
	String hash_file;

	MetadataClass(int n, String path, File f) throws UnknownHostException {
		this.f = f;
		optionRecieved = n;
		this.path = path;
	}

	MetadataClass(String hash) throws UnknownHostException {
		hash_file = hash;
		optionRecieved = 2;
	}

	@Override
	public void run() {

		switch (optionRecieved) {

		// Publish File or Hash from Node
		case 1:

			// 1. make hash of file.
			// 2. find the content-type
			// 3. find the content-size
			// 4. publish file
			// 5. update peers
			try {
				ArrayList<String> metadataContent = new ArrayList<String>();
				String fileHash = new String(createSha1(f).toLowerCase());
				long fileSize = f.length();
				String[] tokens = path.split("\\.(?=[^\\.]+$)");
				String extensionType = tokens[1];
				metadataContent.add(extensionType);
				metadataContent.add(Long.toString(fileSize));
				metadataContent.add(Integer.toString(ACN.port));

				StringBuilder sb = new StringBuilder();
				sb.append(fileHash);
				sb.append("--");
				sb.append(ACN.selfName);
				String hashKey = sb.toString();

				ACN.contents.put(hashKey, metadataContent);
				ACN.storeFileHash.put(fileHash, f.getAbsolutePath());

				extras ex1 = new extras();
				ex1.typeCheck = "fromPublish";
				ex1.hash = hashKey;
				ex1.CTT = ACN.contents;
				ex1.arr = metadataContent;

				Socket sc;
				OutputStream outStream;
				ObjectOutputStream objOutStream;
				// Node Sends Newly Published Content to its connected Peers
				for (Map.Entry<String, Integer> entry : ACN.peers.entrySet()) {
					String key = entry.getKey();
					Integer value = entry.getValue();
					sc = new Socket(key, value);
					outStream = sc.getOutputStream();
					objOutStream = new ObjectOutputStream(outStream);
					objOutStream.writeObject(ex1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		// Unpublish File or Hash from Node
		case 2:
			// 1. get hash of file.
			// 2. unpublish file
			// 3. update peers
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(hash_file);
				sb.append("--");
				sb.append(ACN.selfName);
				String hashKey = sb.toString();

				ACN.contents.remove(hashKey);

				extras ex1 = new extras();
				ex1.hash = hashKey;
				ex1.typeCheck = "fromUnpublish";

				Socket sc;
				OutputStream outStream;
				ObjectOutputStream objOutStream;
				// Node sending newly unpublished data to its connected peers
				for (Map.Entry<String, Integer> entry : ACN.peers.entrySet()) {
					String key = entry.getKey();
					Integer value = entry.getValue();
					sc = new Socket(key, value);
					outStream = sc.getOutputStream();
					objOutStream = new ObjectOutputStream(outStream);
					objOutStream.writeObject(ex1);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
	}

	// Calculate SHA1 of requested file
	public String createSha1(File file) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-1");
		InputStream fis = new FileInputStream(file);
		int n = 0;

		byte[] buffer = new byte[8192];
		while (n != -1) {
			n = fis.read(buffer);
			if (n > 0) {
				digest.update(buffer, 0, n);
			}
		}
		fis.close();
		return new HexBinaryAdapter().marshal(digest.digest());
	}
}
