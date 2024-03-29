
import java.io.*;
import java.util.*;

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class PeerInfoReader {

    /**
     * List of all PEERS from the configuration file.
     */
    public static final HashMap<Integer, Peer> PEERS = new HashMap<>();

    public PeerInfoReader(String peerInfoFilePath) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(peerInfoFilePath));

        String line;
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(" ");
            Peer n = new Peer();
            n.pid = Integer.parseInt(tokens[0]);
            n.hostname = tokens[1];
            n.listeningPort = Integer.parseInt(tokens[2]);
            n.hasFile = Integer.parseInt(tokens[3]);
            // Create bitfield which defaults to all 0s.
            byte[] bitfield = new byte[(int) Math.ceil(CommonConfigReader.getInstance().numPieces / 8.0)];
            // If a peer hasFile, the bits need to be set to 1, the spare bits need to be set to 0 later. 
            if (n.hasFile == 1) {
                Arrays.fill(bitfield, (byte) 0xFF);
            }
            // Create the BitSet.
            n.bitfield = BitSet.valueOf(bitfield);
            // Then, set the spare bits to 0.
            int spareBitCount = bitfield.length * 8 - CommonConfigReader.getInstance().numPieces;
            n.bitfield.clear(/*from*/CommonConfigReader.getInstance().numPieces,
                    /*to*/ CommonConfigReader.getInstance().numPieces + spareBitCount);

            PEERS.put(n.pid, n);
        }

        System.out.println("Finished reading " + peerInfoFilePath);
    }

}
