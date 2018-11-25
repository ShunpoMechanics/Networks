/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class Peer {

    int pid;
    String hostname;
    int listeningPort;
    int hasFile;
    boolean isChoked;
    boolean isPreferred;
    boolean isInterrested;

    int downloadRate;

    public void choke() {
        this.isChoked = true;
    }

    public void unchoke() {
        this.isChoked = false;
    }

    public void prefer() {
        this.isPreferred = true;
    }

    public void notPrefer() {
        this.isPreferred = false;
    }

    public void interested() {
        this.isInterrested = true;
    }

    public void disinterested() {
        this.isInterrested = false;
    }

    public boolean getPreferredStatus() {
        return isPreferred;
    }

    public boolean getInterestStatus() {
        return isInterrested;
    }

    public boolean getChokeStatus() {
        return isChoked;
    }

    public int getStatus() {
        int choke = 0;
        int interrested = 0;
        int preferreded = 0;
        int statusCode = 0;
        if (isChoked) {
            choke = 1;
        }
        if (isInterrested) {
            interrested = 1;
        }
        if (isPreferred) {
            preferreded = 1;
        }
        choke = choke * 100;
        interrested = interrested * 10;
        statusCode = choke + interrested + preferreded;
        return statusCode;
        //Returns a status code with all 3 fields in order to minimize function calls
        //Ex: if unchoked, uninterrested, but preferreded the statusCode returns 001
        //If choked, interrested and preferreded it returns 111
        //Define behavior with a switch using the various combincations instead of if(true && true || false) etc
    }

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
