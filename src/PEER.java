import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;

public class PEER implements Runnable, Serializable {

	int port;
	String hostname;

	PEER(String hostname, String port) throws UnknownHostException {
		this.hostname = hostname;
		this.port = Integer.parseInt(port);
	}

	@Override
	public void run() {
		// Sends peer request along with its Contents to the peering node
		try {
			extras ex3 = new extras();			
			ACN.peers.put(hostname, port);
			ex3.typeCheck = "fromPeer";
			ex3.selfName = ACN.selfName;
			ex3.port = ACN.port;
			ex3.CTT = ACN.contents;
			Socket sock = new Socket(hostname, port);
			OutputStream outStream = sock.getOutputStream();
			ObjectOutputStream objOutStream = new ObjectOutputStream(outStream);
			objOutStream.writeObject(ex3);
			sock.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
