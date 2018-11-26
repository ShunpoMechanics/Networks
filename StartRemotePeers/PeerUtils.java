
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class PeerUtils {

    public static Message generateInterestMessageTo(Peer remote) throws Exception {
        // Update the `interest` booleans.
        remote.currentClientInterestedInPeer = true;
        return new Message(0, Message.MessageType.interested, null);
    }

    public static Message generateRequestMessageFrom(int local_pid) throws Exception {
        Peer p = PeerInfoReader.PEERS.get(local_pid);
        CommonConfigReader ccr = CommonConfigReader.getInstance();
        Random rand = new Random();
        int selectedIndex = rand.nextInt(ccr.numPieces);
        // If the selectedIndex is already set to 1, client has the piece, so try again.
        // Since the index is random, if the piece has not been received yet,
        // it is possible but unlikely to send the same request to multiple peers.
        int lastRandom;
        while (p.bitfield.get(selectedIndex)) {
            lastRandom = selectedIndex;
            // Just get the next clear bit, this way the random loop won't be repeated too many times.
            selectedIndex = p.bitfield.nextClearBit(selectedIndex);
            // If nextClearBit was not found or was one of the spare bits, try a new random index smaller than the last random.
            if (selectedIndex == p.bitfield.length() || selectedIndex >= ccr.numPieces) {
                selectedIndex = rand.nextInt(lastRandom);
            }
        }
        byte[] payload = ByteBuffer.allocate(4).putInt(selectedIndex).array();
        return new Message(payload.length, Message.MessageType.request, payload);
    }

    /**
     * Connect to others PEERS with pid lower that current client's pid.
     */
    public static CopyOnWriteArrayList<Connection> connectToOthers(int local_pid) {
        // A thread-safe variant of ArrayList, which is very efficient if the number of mutations is low like this program.
        CopyOnWriteArrayList<Connection> conns = new CopyOnWriteArrayList<>();
        // Connect to other PEERS with pid lower than this peer's pid.
        for (Integer id : PeerInfoReader.PEERS.keySet()) {
            if (id < local_pid) {
                Peer neighbor = PeerInfoReader.PEERS.get(id);
                try {
                    Socket s = new Socket(neighbor.hostname, neighbor.listeningPort);
                    Connection conn = new Connection(s,
                            new ObjectOutputStream(s.getOutputStream()),
                            new ObjectInputStream(s.getInputStream()),
                            local_pid);
                    // Keep track of connected PEERS.
                    conns.add(conn);
                    System.out.println("pid " + local_pid + ": opened connection to " + neighbor.hostname + ":" + neighbor.listeningPort);
                } catch (IOException ex) {
                    System.out.println("Socket creation to " + neighbor.hostname + ":" + neighbor.listeningPort + " failed");
                    Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return conns;
    }

    /**
     * @param k
     *
     * @return
     */
    public static TwoLists choosePreferredNeighbors(int k) {
        List<Peer> interestedNeighbors = new ArrayList<>();
        List<Peer> unselectedNeighbors = new ArrayList<>();
        List<Peer> preferredNeighbors = new ArrayList<>(k + 1);

        for (Integer pid : PeerInfoReader.PEERS.keySet()) {
            Peer p = PeerInfoReader.PEERS.get(pid);
            // A neighbor can become a preferred neighbor only if it's interested.
            if (p.peerInterestedInCurrentClient) {
                interestedNeighbors.add(p);
            } else {
                unselectedNeighbors.add(p);
            }
        }
        // Sort based on download rates in descending order.
        Collections.sort(interestedNeighbors, Comparator.comparing(Peer::getDownloadRate).reversed());
        // Take the top K, these are definitely preferred.
        for (int i = 0; i < k; i++) {
            preferredNeighbors.add(interestedNeighbors.get(i));
        }
        // Add the rest of interested neighbors to unselected.
        for (int i = k; i < interestedNeighbors.size(); i++) {
            unselectedNeighbors.add(interestedNeighbors.get(i));
        }
        // However, choose one random neighbor from the interested, but not in the top k, neighbors as well.
        Random rand = new Random();
        Peer optimisticallySelected = interestedNeighbors.get(rand.nextInt(interestedNeighbors.size() - k) + k /* Random in range k to interestedNeighbors.size() */);
        preferredNeighbors.add(optimisticallySelected); // Keep in preferred and remove from unselected.
        unselectedNeighbors.remove(optimisticallySelected);

        // Return both preferredNeighbors (to send "unchoke" to) and unselectedNeighbors (to send "choke" to).
        PeerUtils.TwoLists res = new PeerUtils.TwoLists(preferredNeighbors, unselectedNeighbors);
        return res;
    }

    public static class TwoLists {

        /**
         * The peers to unchoke are the k peer selected based on download rates
         * and one random.
         */
        List<Peer> peersToUnchoke;
        /**
         * All other peers must be choked.
         */
        List<Peer> peerToChoke;

        public TwoLists(List<Peer> peersToUnchoke, List<Peer> peerToChoke) {
            this.peersToUnchoke = peersToUnchoke;
            this.peerToChoke = peerToChoke;
        }

    }

}
