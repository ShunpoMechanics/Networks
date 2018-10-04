import java.util.*;
import java.io.*;
import java.nio.file.*;
import static java.nio.file.StandardOpenOption.*;
import java.text.*;
public class Log {
	/* Each peer should write its log into the log file ‘log_peer_[peerID].log’ 
	at the working directory. For example, the peer with peer ID 1001 should write 
	its log into the file ‘~/project/log_peer_1001.log’.
	*/

	// main file for testing only
	public static void main(String args[]) 
	{
		TCPto(1001,1002);
		TCPfrom(1002,1001);
		int [] testPeers = {1001,1004,1006,1009};
		preferredNeighbors(1002, testPeers);
		optimimisticallyUnchoked(1002,1003);
		unchoked(1003,1002);
		choked(1006,1009);
		have(1004,1006,3);
		interested(1006,1004);
		notInterested(1006,1009);
		download(1002,1003,3,2);
		complete(1002);
	}

	// TCP Connection
	public static void TCPto(int peer1, int peer2) {
	    String message = peer1 + " makes a connection to Peer " + peer2 + ".";
	    wrtieLog(peer1, message);
	}

	public static void TCPfrom(int peer1, int peer2) {
	    String message = peer1 + " is connected from Peer " + peer2 + ".";
	    wrtieLog(peer1, message);
	}

	// Preferred Neighbors
	public static void preferredNeighbors(int peer1, int [] neighbors) {
		String message = peer1 + " has the preferred neighbors ";
		for (int i = 0; i < neighbors.length - 1; i++) {
			message += neighbors[i] + ",";
		}
		message += neighbors[neighbors.length-1] + ".";
		wrtieLog(peer1, message);
	}

	// Optimistically Unchoked Neighbor
	public static void optimimisticallyUnchoked(int peer1, int peer2) {
		String message = peer1 + " has the optimistically unchoked neighbor " + peer2 + ".";
	    wrtieLog(peer1, message);
	}

	// Unchoking
	public static void unchoked(int peer1, int peer2) {
		String message = peer1 + " is unchoked by " + peer2 + ".";
	    wrtieLog(peer1, message);
	}

	// Choking
	public static void choked(int peer1, int peer2) {
		String message = peer1 + " is choked by " + peer2 + ".";
	    wrtieLog(peer1, message);
	}

	// Receiving 'have' message
	public static void have(int peer1, int peer2, int pieceIndex) {
		String message = peer1 + " received the 'have' message from " + peer2 + " for the piece " + pieceIndex + ".";
	    wrtieLog(peer1, message);
	}

	// Receiving 'interested' message
	public static void interested(int peer1, int peer2) {
		String message = peer1 + " received the 'interested' message from " + peer2 + ".";
	    wrtieLog(peer1, message);
	}

	// receiving 'not interested' message
	public static void notInterested(int peer1, int peer2) {
		String message = peer1 + " received the 'not interested' message from " + peer2 + ".";
	    wrtieLog(peer1, message);
	}

	// downloading a piece
	public static void download(int peer1, int peer2, int pieceIndex, int numPieces) {
		String message = peer1 + " has downloaded the piece " + pieceIndex + " from " + peer2 + "." 
		+ "\nNow the number of pieces it has is " + numPieces + ".";
	    wrtieLog(peer1, message);
	}

	// completion of download
	public static void complete(int peer1) {
		String message = peer1 + " has downloaded the complete file.";
	    wrtieLog(peer1, message);
	}

	// Generic write to log file
	public static void wrtieLog (int peer1, String message) {
		String fileName = "log_peer_" + peer1 + ".log";
		Path p = Paths.get(fileName);
		Date date = new Date();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		String sDate = df.format(date);
		String log = "\n" + sDate + ": Peer " + message;
		byte [] logBytes = log.getBytes();
		try (
			OutputStream out = Files.newOutputStream(p, CREATE, APPEND)) {
				out.write(logBytes);
				System.out.print(log); // remove this before turning in
	    } catch (IOException x) {
	      System.err.println(x);
	    }
	}

}