import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class extras implements Serializable {

	/*
	 * Temporary storage buffer while creating and sending objects to
	 * connected/connecting peers
	 */

	public String hash;
	public String typeCheck;
	public String selfName;
	public int port;
	public HashMap<String, ArrayList<String>> CTT = new HashMap<String, ArrayList<String>>();
	public ArrayList<String> arr = new ArrayList<String>();
}
