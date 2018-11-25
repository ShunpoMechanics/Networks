import java.io.*;
import java.util.*;

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class PeerInfoReader {

    public static void main(String args[]) throws IOException {
        PeerInfoReader ss = new PeerInfoReader("PeerInfo.cfg");
    }
    
    /**
     *   List of all peers from the Common.
     */
    public static final HashMap<Integer, Peer> peers = new HashMap<>();

    public PeerInfoReader(String peerInfoFilePath) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(peerInfoFilePath));

        String line = "";
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(" ");
            Peer n = new Peer();
            n.pid = Integer.parseInt(tokens[0]);
            n.hostname = tokens[1];
            n.listeningPort = Integer.parseInt(tokens[2]);
            n.hasFile = Integer.parseInt(tokens[3]);
            peers.put(n.pid, n);
        }

        Flags.print("Finished reading " + peerInfoFilePath, Flags.Debug.INFO);
        Flags.print(this.toString(), Flags.Debug.INFO);
    }

}
