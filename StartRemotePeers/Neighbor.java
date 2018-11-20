/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class Neighbor {

    int id;
    String hostname;
    int listeningPort;
    int hasFile;
    
    int downloadRate;

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
}
