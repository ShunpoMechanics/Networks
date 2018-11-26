
import java.util.*;
import java.util.stream.Stream;

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class PeerUtils {

    /**
     *
     * @param k
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
