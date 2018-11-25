
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
public class PeerUtils {

    public static List<Peer> choosePreferredNeighbors(int k, List<Peer> neighbors) {
        List<Peer> preferredNeighbors = new ArrayList<>(k);
        // Sort based on download rates in descending order.
        Collections.sort(neighbors, Comparator.comparing(Peer::getDownloadRate).reversed());
        // Take the top K.
        for (int i = 0; i < k; i++) {
           preferredNeighbors.add(neighbors.get(i));
        }
        // Choose one random neighbor from the unselected neighbors as well.
        Random rand  = new Random();
        preferredNeighbors.add(neighbors.get(rand.nextInt(neighbors.size()-k) + k  /* Random in range k to neighbors.size() */));
        return preferredNeighbors;
    }
}
