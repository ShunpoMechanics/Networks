
import java.io.*;
import java.util.*;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class PeerInfoReader {

    public static void main(String args[]) throws IOException {
        PeerInfoReader ss = new PeerInfoReader("../LocalPeerInfo.cfg");
    }
    
    HashMap<Integer, Neighbor> neighbors = new HashMap<Integer, Neighbor>();

    public PeerInfoReader(String peerInfoFilePath) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(peerInfoFilePath));

        String line = "";
        while ((line = br.readLine()) != null) {
            String[] tokens = line.split(" ");
            Neighbor n = new Neighbor();
            n.id = Integer.parseInt(tokens[0]);
            n.hostname = tokens[1];
            n.listeningPort = Integer.parseInt(tokens[2]);
            n.hasFile = Integer.parseInt(tokens[3]);
            neighbors.put(n.id, n);
        }

        Flags.print("Finished reading " + peerInfoFilePath, Flags.Debug.INFO);
        Flags.print(this.toString(), Flags.Debug.INFO);
    }

}
