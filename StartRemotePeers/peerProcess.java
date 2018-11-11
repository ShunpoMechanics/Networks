/**
 *
 * @author Kimberly Ying (kimying@ufl.edu)
 */

import java.util.*;
import java.io.*;

public class peerProcess {

	static int numPieces;
	static int numPreferredNeighbors;
	static int unchokingInterval;
	static int optUnchokingInterval;
	static String fileToDownload;
	static int fileSize;
	static int pieceSize;

	// chokedUnchoked key value pair (int peerID, boolean choke)
	// bitfields key value pair (int peerID, bitSet bitfield)

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Must have one argument for peerID.");
			System.exit(0);
		}
		int peerID = Integer.parseInt(args[0]);

		String st = "";
		String line;
		// set variables according to config file
		try {
			BufferedReader in = new BufferedReader(new FileReader("Common.cfg"));
			while((line = in.readLine()) != null) {
				st += line + " ";
			}
			String[] tokens = st.split("\\s+");
			for (String token : tokens) {
				System.out.print(token + " ");
			}
			numPreferredNeighbors = Integer.parseInt(tokens[1]);
			unchokingInterval = Integer.parseInt(tokens[3]);
			optUnchokingInterval = Integer.parseInt(tokens[5]);
			fileToDownload = tokens[7];
			fileSize = Integer.parseInt(tokens[9]);
			pieceSize = Integer.parseInt(tokens[11]);
			numPieces = (int)Math.ceil((double)fileSize/pieceSize);
			in.close();
		}
		catch (Exception ex) {
			System.out.println(ex.toString());
		}

		// while not all pieces have complete file
		// TCP connection
		// send handshake
		// receive and check handshake (check if header is right, expected peer ID)
		// send bitfield if has pieces
		// receive bitfield if has pieces
		// check bitfield message 
		// if peer B has piece peer A doesn't, then send interested message
		// else send not interested message 

		// if num seconds (%) mod unchoking interval == 0
		// determine preferred neighbors
		// send unchoke message to all preferred neighbors that aren't already unchoked
		// send choke message to any neighbors that are unchoked but not still preferred neighbors

		// if num seconds (%) mod optUnchokingInterval == 0
		// optimistically unchoked

		// if receive bitfield or have message from neighbor, determine whether to send 'interested' message back
		// update neighbor bitfields if receive have message
		// if neighbor has no interesting pieces, sends not interested message
		// when receives piece completely, check bitfields of neightbors and send not interested if necessary

		// for all unchoked, while unchoked, send request message for piece, when piece downloads completely, send another request message, etc

	}

	// handshake function


	// actual message function - 4-byte message length, 1-byte message type, variable message payload


	// choke - message type 0, no payload


	// unchoke - message type 1, no payload


	// interested - message type 2, no payload


	// not interested - message type 3, no payload


	// have - message type 4, payload is 4-byte piece index field


	// bitfield - message type 5, payload is bitfield
	// sent as the first message after handshaking if has pieces of the file



	// request - message type 6, payload is 4-byte piece index field


	// piece - message type 7, payload is 4-byte piece index field and content of piece

	public static boolean hasCompleteFile(int id) {
		return true;
	}

	public static void setBitfield() {

	}

	public static int [] determinePreferredNeighbors() {
		int [] neighbors = new int [numPreferredNeighbors]; // (k)
		// if has complete file, determine peers randomly
		// for all interested neighbors (check from peerInfoVector)
		// calculate download rate and keep track of top k, settle ties randomly

		return neighbors;
	}

	public static void requestPiece(int id) {
		int piece;
		// find all needed pieces with all pieces that peer id has
		// randomly select piece to request
		// call request message function
	}


}