/*
 *                     CEN5501C Project2
 * This is the program starting remote processes.
 * This program was only tested on CISE SunOS environment.
 * If you use another environment, for example, linux environment in CISE 
 * or other environments not in CISE, it is not guaranteed to work properly.
 * It is your responsibility to adapt this program to your running environment.
 */

import java.util.*;

public class RemotePeerInfo {
	public String peerId;
	public String peerAddress;
	public String peerPort;
	public boolean hasFile;
	public int numPieces;
	public BitSet pieceMap;
	
	public RemotePeerInfo(String pId, String pAddress, String pPort) {
		peerId = pId;
		peerAddress = pAddress;
		peerPort = pPort;
	}

	public RemotePeerInfo(String pId, String pAddress, String pPort, boolean hasFile, int numPieces) {
		peerId = pId;
		peerAddress = pAddress;
		peerPort = pPort;
		this.hasFile = hasFile;
		this.numPieces = numPieces;
		pieceMap = new BitSet(numPieces);
		// set bits to all 1s for has file and all 0s for doesn't have file
	}

	// should we add here hasFile, bitArray that indicates pieces of interest?
}
