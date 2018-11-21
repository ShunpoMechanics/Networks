/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 * @author Kim Ying (kimying@ufl.edu)
 */

import java.util.*;

public class Neighbor {
	private final static byte[] zeroBits = new byte[10];
    int id;
    String hostname;
    int listeningPort;
    int hasFile;
    boolean connected = false;
    int downloadRate;
    BitSet pieces;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    public String getHostname() {
    	return this.hostname;
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

    public int getListeningPort() {
    	return this.listeningPort;
    }

    public boolean hasFile() {
    	return (this.hasFile == 1);
    }

    public void setFileComplete() {
    	this.hasFile = 1;
    }

    public void setConnection(boolean connect) {
    	this.connected = connect;
    }

    public boolean isConnected() {
    	return this.connected;
    }

    public String getHandshakeHeader() {
    	return "P2PFILESHARINGPROJ" + new String(zeroBits) + this.id;
    }

    public void initializePieceSet(int size) {
    	this.pieces = new BitSet(size);
    	if (this.hasFile()) { // if has complete file, set all bits to true
    		pieces.set(0, size);
    	}
    }

    public void setPieceIndex(int index) {
    	pieces.set(index);
    }

    // this is called whenever a peer receives "have" message from neighbor
    public void updatePieceSet(BitSet bitset) {
    	this.pieces = bitset;
    }
}
