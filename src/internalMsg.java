
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

public class internalMsg implements Runnable, Serializable 
{

	@Override
	public void run() 
	{
		try 
		{
			ServerSocket servSock = new ServerSocket(ACN.port);
			extras ex2 = new extras();
			while (true) 
			{
				Socket socket = servSock.accept();
				InputStream inStream = socket.getInputStream();
				ObjectInputStream objInStream = new ObjectInputStream(inStream);
				ex2 = (extras) objInStream.readObject();
				
				switch (ex2.typeCheck) {
				
				// Peering node checks for difference in contents table				
				case "fromPeerCase":
					HashMap<String, ArrayList<String>> difference = new HashMap<String, ArrayList<String>>();
					for (Map.Entry<String, ArrayList<String>> entry : ex2.CTT.entrySet()) 
					{
						String key = entry.getKey();
						ArrayList<String> value = entry.getValue();
						if (!(ACN.contents.containsKey(key))) 
						{
							difference.put(key, value);
						}
					}
					if (difference.isEmpty())
						break;

					for (Map.Entry<String, ArrayList<String>> entry : difference.entrySet()) {
						String key = entry.getKey();
						ArrayList<String> value = entry.getValue();
						ACN.contents.put(key, value);
						
						extras ex1 = new extras();
						ex1.hash = key;
						ex1.arr = value;
						ex1.typeCheck = "fromPublish";
						Socket sc1;
						OutputStream os;
						ObjectOutputStream oos;
						for (Map.Entry<String, Integer> e1 : ACN.peers.entrySet()) {
							String key1 = e1.getKey();
							int value1 = e1.getValue();
							if (key1.equals(ex2.selfName))
								continue;
							sc1 = new Socket(key1, value1);
							os = sc1.getOutputStream();
							oos = new ObjectOutputStream(os);
							oos.writeObject(ex1);
						}
					}
					break;

					/*
					 * Peering node adds the node it is peering to in its peers list,
					 * and sends its metadata content to its connecting peer
					 */
				case "fromPeer":
					difference = new HashMap<String, ArrayList<String>>();
					ACN.peers.put(ex2.selfName, ex2.port);
					extras ex3 = new extras();
					ex3.typeCheck = "fromPeerCase";
					ex3.selfName = ACN.selfName;
					ex3.port = ACN.port;
					ex3.CTT = ACN.contents;
					Socket sc2 = new Socket(ex2.selfName, ex2.port);
					OutputStream os2 = sc2.getOutputStream();
					ObjectOutputStream oos2 = new ObjectOutputStream(os2);
					oos2.writeObject(ex3);

					for (Map.Entry<String, ArrayList<String>> entry : ex2.CTT.entrySet()) {
						String key = entry.getKey();
						ArrayList<String> value = entry.getValue();
						if (!(ACN.contents.containsKey(key))) {
							difference.put(key, value);
						}
					}
					if (difference.isEmpty())
						break;
					for (Map.Entry<String, ArrayList<String>> entry : difference.entrySet()) {
						String key = entry.getKey();
						ArrayList<String> value = entry.getValue();
						ACN.contents.put(key, value);
						extras ex1 = new extras();
						// msg1.CTT.put(key, value);
						ex1.hash = key;
						ex1.arr = value;
						ex1.typeCheck = "fromPublish";
						Socket sc1;
						OutputStream os;
						ObjectOutputStream oos;
						for (Map.Entry<String, Integer> e1 : ACN.peers.entrySet()) {
							String key1 = e1.getKey();
							int value1 = e1.getValue();
							if (key1.equals(ex2.selfName))
								continue;
							sc1 = new Socket(key1, value1);
							os = sc1.getOutputStream();
							oos = new ObjectOutputStream(os);
							oos.writeObject(ex1);
						}

					}
					break;
					
					/*
					 * On receiving publish command,
					 * it adds the file to its contents table
					 * and sends this newly added file information to its peers.
					 */
				case "fromPublish":
					if (!ACN.contents.containsKey(ex2.hash)) {
						ACN.contents.put(ex2.hash, ex2.arr);
						ex2.typeCheck = "fromPublish";
						Socket sc;
						OutputStream os;
						ObjectOutputStream oos;

						for (Map.Entry<String, Integer> entry : ACN.peers.entrySet()) {
							String key = entry.getKey();
							Integer value = entry.getValue();
							String[] str = ex2.hash.split("--");
							if (key.equals(str[1]))
								continue;
							ex2.selfName = ACN.selfName;
							sc = new Socket(key, value);
							os = sc.getOutputStream();
							oos = new ObjectOutputStream(os);
							oos.writeObject(ex2);
						}
					}
					break;

					/*
					 * On receiving unpublish command, 
					 * it removes the hash to be unpublished from its table
					 * and sends this information to its peers having this hash value
					 */	
				case "fromUnpublish":
					if (ACN.contents.containsKey(ex2.hash)) {
						ACN.contents.remove(ex2.hash);
						Socket sc1;
						OutputStream os;
						ObjectOutputStream oos;
						for (Map.Entry<String, Integer> e1 : ACN.peers.entrySet()) {
							String key1 = e1.getKey();
							int value1 = e1.getValue();
							String[] str = ex2.hash.split("--");
							if (key1.equals(str[1]))
								continue;
							ex2.selfName = ACN.selfName;
							sc1 = new Socket(key1, value1);
							os = sc1.getOutputStream();
							oos = new ObjectOutputStream(os);
							oos.writeObject(ex2);
						}
					}
					break;
					
					/*Since the requested node does not have the file
					 * the node calls this code 
					 * to request for the file from its peers
					 */
				case "fromGateway":
					String hash = ex2.hash;
					File file = new File(ACN.storeFileHash.get(hash));
					Socket sc1 = new Socket(ex2.selfName, ex2.port);
					OutputStream os = sc1.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(os);
					oos.writeObject(file);
					break;

				default:
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
