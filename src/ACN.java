import java.io.File;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.io.Serializable;

public class ACN implements Serializable {
	
	/*
	 * Initializing variables for 
	 * storing important data.
	 * Accepting multithreading requests:- Upto 200 concurrent connections
	 */

	public static HashMap<String, Integer> peers = new HashMap<String, Integer>();
	public static HashMap<String, ArrayList<String>> contents = new HashMap<String, ArrayList<String>>();
	public static HashMap<String, String> storeFileHash = new HashMap<String, String>();
	public static int port;
	public static String selfName;

	public static void main(String[] args) throws Exception {

		Scanner scan = new Scanner(System.in);
		selfName = InetAddress.getLocalHost().getHostName();
		ACN.port = Integer.parseInt(args[0]);

		ExecutorService thread = Executors.newFixedThreadPool(200);
		Runnable gatewayRun = new gateway();
		thread.execute(gatewayRun);
		Runnable internalMsgRun = new internalMsg();
		thread.execute(internalMsgRun);

		while (true) {
			System.out.println("\nEnter Valid Commands: Enter EXIT to quit the program\n");
			System.out.println("PEER <Hostname> <Port>");
			System.out.println("SHOW_PEERS");
			System.out.println("PUBLISH <File_Name>");
			System.out.println("UNPUBLISH <Hash>");
			System.out.println("SHOW_PUBLISHED");
			System.out.println("SHOW_METADATA\n");
			String in = scan.nextLine();
			in = in.trim();
			String input[] = in.split(" ");
			switch (input[0].toUpperCase()) {

			case "PEER":
				Runnable peerRun = new PEER(input[1], input[2]);
				thread.execute(peerRun);
				break;

			case "PUBLISH":
				File file = new File(input[1]);
				Runnable metarunPub = new MetadataClass(1, input[1], file);
				thread.execute(metarunPub);
				break;

			case "UNPUBLISH":
				Runnable metarunUnPub = new MetadataClass(input[1]);
				thread.execute(metarunUnPub);
				break;

			case "SHOW_PEERS":
				for (Map.Entry<String, Integer> entry : peers.entrySet()) {
					String key = entry.getKey();
					Integer value = entry.getValue();
					StringBuilder sb = new StringBuilder();
					sb.append(key);
					sb.append(": ");
					sb.append(value);
					String peer = sb.toString();
					System.out.println(peer);
					sb.setLength(0);
				}
				break;

			case "SHOW_METADATA":
				int countMeta=0;
				for (Map.Entry<String, ArrayList<String>> entry : contents.entrySet()) {
					countMeta++;
					String key = entry.getKey();
					ArrayList<String> value = entry.getValue();
					for (Map.Entry<String, String> entry2 : storeFileHash.entrySet()) {
						String key2 = entry2.getKey();
						String[] str = key.split("--");
						if(str[0].equals(key2)){
							String value2 = entry2.getValue();
							String[] str2 = value2.split("/");
							int length = str2.length;
							System.out.println(key + ": " + str2[length-1]+": "+value);
							break;
						}
					}
				}
				if(countMeta==0)
					System.out.println("\nNothing yet shared with you or by you.\n");
				break;

			case "SHOW_PUBLISHED":
				int countPub=0;
				for (Map.Entry<String, ArrayList<String>> entry : contents.entrySet()) {
					String key = entry.getKey();
					ArrayList<String> value = entry.getValue();
					String[] str = key.split("--");
					if (str[1].equals(selfName)){
						countPub++;
						for (Map.Entry<String, String> entry2 : storeFileHash.entrySet()) {
							String key2 = entry2.getKey();
							if(str[0].equals(key2)){
								String value2 = entry2.getValue();
								String[] str2 = value2.split("/");
								int length = str2.length;
								System.out.println(str[0] + ": " + str2[length-1]+": "+value);
								break;
							}
						}
					}
				}
				if(countPub==0)
					System.out.println("\nNothing Published Yet. Please publish before checking\n");
				break;

			case "EXIT":
				thread.shutdownNow();
				System.exit(0);

			default:
				System.out.println("Please enter only Valid Cases! Thank You!");
			}
		}
	}
}