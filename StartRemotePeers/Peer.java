
import java.util.BitSet;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class Peer {

    int pid;
    String hostname;
    int listeningPort;
    int hasFile;
    boolean isPreferred;
    // Source https://wiki.theory.org/index.php/BitTorrentSpecification#Peer_wire_protocol_.28TCP.29 for the following:
    // ``Client connections start out as "choked" and "not interested".``
    boolean peerChokedCurrentClient = true; // Peer is choking the current client.
    boolean currentClientChokedPeer = true; // The current client is choking the peer.
    boolean peerInterestedInCurrentClient = false; // Peer is interested in the current client.
    boolean currentClientInterestedInPeer = false; // The current client is interested in the peer.

    AtomicInteger downloadRate = new AtomicInteger(0);
    // If the peer has the entire file, this would be initialized to 1s (except spare bits), otherwise this starts at all 0s.
    // As the peer receives pieces, this is updated accordingly.
    BitSet bitfield;
    // To keep track of indices that have been requested, but not received (and thus might not be 1 on the bitfield yet).
    // This is no longer needed, as we randomly select pieces (and could possibly repeat the request if not received).
//    HashSet<Integer> requestedIndices;

    /**
     * @return the pid
     */
    public int getPid() {
        return pid;
    }

    /**
     * @param pid the pid to set
     */
    public void setPid(int pid) {
        this.pid = pid;
    }

    /**
     * @return the downloadRate
     */
    public int getDownloadRate() {
        return downloadRate.get();
    }

    /**
     * @param downloadRate the downloadRate to set
     */
    public synchronized void setDownloadRate(int downloadRate) {
        this.downloadRate = new AtomicInteger(downloadRate);
    }
}
