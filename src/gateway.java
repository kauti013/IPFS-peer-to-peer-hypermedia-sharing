import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

public class gateway implements Runnable, Serializable {
	@Override
	public void run() {

		/*
		 * Accepts client browser connections on port 8888
		 */

		try {
			int port = 8888;
			ServerSocket servSock = new ServerSocket(port);
			while (true) {
				Socket sc = servSock.accept();
				BufferedReader br = new BufferedReader(new InputStreamReader(sc.getInputStream()));
				String eachLine = br.readLine();

				String[] url;
				url = eachLine.split(" ");
				String urlHash = url[1];
				urlHash = urlHash.substring(1);
				/*
				 * Checks if the requested Peer node has the file Later checks
				 * if the file is of type HTML or PNG : depending on the type,
				 * the request is handled.
				 */
				if (ACN.contents.containsKey(urlHash + "--" + ACN.selfName)) {
					File file = new File(ACN.storeFileHash.get(urlHash));
					PrintWriter out = new PrintWriter(sc.getOutputStream());
					OutputStream outStream = sc.getOutputStream();
					DataOutputStream dataStream;
					if ((ACN.contents.get(urlHash + "--" + ACN.selfName)).get(0).equals("html")) {
						BufferedReader br1 = new BufferedReader(new FileReader(ACN.storeFileHash.get(urlHash)));
						String line1;
						String content = "";
						while ((line1 = br1.readLine()) != null) {
							content += line1;
						}
						out.println("HTTP/1.1 200 OK\r\n");
						out.println("Content-Type: text/html\r\n");
						out.println("\r\n");
						out.print(content);
						out.close();
					} else {
						FileInputStream fis = new FileInputStream(file);
						byte[] data = new byte[(int) file.length()];
						fis.read(data);
						fis.close();

						dataStream = new DataOutputStream(outStream);
						dataStream.writeBytes("HTTP/1.0 200 OK\r\n");
						dataStream.writeBytes("Content-Type: image/png\r\n");
						dataStream.writeBytes("Content-Length: " + data.length);
						dataStream.writeBytes("\r\n\r\n");
						dataStream.write(data);
						out.close();
						dataStream.close();
					}
				}
				/*
				 * If the file is not hosted by the request peer The peer checks
				 * in its contents to see if any of its peer have uploaded the
				 * document If uploaded checks for file type and handles request
				 */
				else {
					int flag = 0;
					for (Map.Entry<String, ArrayList<String>> entry : ACN.contents.entrySet()) {
						String key = entry.getKey();
						ArrayList<String> value = entry.getValue();
						String str[] = key.split("--");
						String hashval = str[0];
						if (hashval.equals(urlHash)) {
							Socket sc1 = new Socket(str[1], Integer.parseInt(value.get(2)));
							OutputStream os = sc1.getOutputStream();
							ObjectOutputStream oos = new ObjectOutputStream(os);

							Random r = new Random();
							Integer rand = r.nextInt(65000 - 10000) + 10000;
							extras ex = new extras();
							ex.typeCheck = "fromGateway";
							ex.hash = urlHash;
							ex.port = rand;
							ex.selfName = ACN.selfName;
							ServerSocket ss1 = new ServerSocket(ex.port);
							oos.writeObject(ex);
							Socket sock = ss1.accept();
							InputStream is = sock.getInputStream();
							ObjectInputStream ois = new ObjectInputStream(is);
							File file = (File) ois.readObject();
							PrintWriter out = new PrintWriter(sc.getOutputStream());
							OutputStream outS = sc.getOutputStream();
							DataOutputStream dataStream;
							if (value.get(0).equals("html")) {
								BufferedReader br1 = new BufferedReader(new FileReader(file));
								String line1 = null;
								String content = "";
								while ((line1 = br1.readLine()) != null) {
									content += line1;
								}
								out.println("HTTP/1.1 200 OK");
								out.println("Content-Type: text/html");
								out.println("\r\n");
								out.print(content);
								out.close();
							} else {
								FileInputStream fis = new FileInputStream(file);
								byte[] data = new byte[(int) file.length()];
								fis.read(data);
								fis.close();

								dataStream = new DataOutputStream(outS);
								dataStream.writeBytes("HTTP/1.0 200 OK\r\n");
								dataStream.writeBytes("Content-Type: image/png\r\n");
								dataStream.writeBytes("Content-Length: " + data.length);
								dataStream.writeBytes("\r\n\r\n");
								dataStream.write(data);
								out.close();
								dataStream.close();
							}
							flag = 1;
							break;
						}
					}
					/*If file is not hosted by any of its peers
					 * Sends back 404 error: file not found
					 */
					if (flag == 0) {
						PrintWriter out = new PrintWriter(sc.getOutputStream());
						out.println("HTTP/1.1 404 Not Found");
						out.println("Content-Type: text/html");
						out.println("\r\n");
						out.print("<html><body><h1>404 Error: <br>File Not Found</h1></body></html>");
						out.close();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
