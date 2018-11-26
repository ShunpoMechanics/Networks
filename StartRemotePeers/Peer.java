
/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class Peer {

    int pid;
    String hostname;
    int listeningPort;
    int hasFile;
    boolean isPreferred;
    // Source https://wiki.theory.org/index.php/BitTorrentSpecification#Peer_wire_protocol_.28TCP.29 for the following:
    boolean peerChokedCurrentClient; // Peer is choking the current client.
    boolean currentClientChokedPeer; // The current client is choking the peer.
    boolean peerInterestedInCurrentClient; // Peer is interested in the current client.
    boolean currentClientInterestedInPeer; // The current client is interested in the peer.

    int downloadRate;
    // If the peer has the entire file, this would be initialized to 1s (except spare bits), otherwise this starts at all 0s.
    // As the peer receives pieces, this is updated accordingly.
    byte[] bitfield;

    /**
     * Set the bit at pieceIndex to 1.
     *
     * @param pieceIndex
     */
    public void updateBitfield(int pieceIndex) {
        // (pieceIndex / 8) is the index of the byte, (pieceIndex % 8) is the index within the byte which is set to one through 
        // a logical "OR". 
        bitfield[(pieceIndex / 8)] |= (0x80 /* 1000 0000 */ >> (pieceIndex % 8));
    }

    public void prefer() {
        this.isPreferred = true;
    }

    public void notPrefer() {
        this.isPreferred = false;
    }

    public boolean getPreferredStatus() {
        return isPreferred;
    }

    
//    public int getStatus() {
//        int choke = 0;
//        int interrested = 0;
//        int preferreded = 0;
//        int statusCode = 0;
//        if (isChoked) {
//            choke = 1;
//        }
//        if (isInterrested) {
//            interrested = 1;
//        }
//        if (isPreferred) {
//            preferreded = 1;
//        }
//        choke = choke * 100;
//        interrested = interrested * 10;
//        statusCode = choke + interrested + preferreded;
//        return statusCode;
//        //Returns a status code with all 3 fields in order to minimize function calls
//        //Ex: if unchoked, uninterrested, but preferreded the statusCode returns 001
//        //If choked, interrested and preferreded it returns 111
//        //Define behavior with a switch using the various combincations instead of if(true && true || false) etc
//    }

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
        return downloadRate;
    }

    /**
     * @param downloadRate the downloadRate to set
     */
    public void setDownloadRate(int downloadRate) {
        this.downloadRate = downloadRate;
    }
}
