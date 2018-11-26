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
	boolean isChoked;
	boolean isPreferred;
	boolean isInterrested;
	
    int downloadRate;
	
	public void choke()
	{this.isChoked = true;}
	
	public void unchoke()
	{this.isChoked = false;}

	public void prefer()
	{this.isPreferred = true;}
	
	public void notPrefer()
	{this.isPreferred = false;}
	
	public void interrested()
	{this.isInterrested = true;}
	
	public void disinterested()
	{this.isInterrested = false;}
	
	public boolean getPreferredStatus()
	{return isPreferred;}
	
	public boolean getInterestStatus()
	{return isInterrested;}
	
	public boolean getChokeStatus()
	{return isChoked;}
	
	public int getStatus()
	{
		int choke = 0;
		int interrested = 0;
		int preferreded = 0;
		int statusCode = 0;
		if(isChoked)
			choke = 1;
		if(isInterrested)
			interrested = 1;
		if(isPreferred)
			preferreded = 1;
		choke = choke*100;
		interrested = interrested*10;
		statusCode = choke + interrested + preferreded;
		return statusCode;
		//Returns a status code with all 3 fields in order to minimize function calls
		//Ex: if unchoked, uninterrested, but preferreded the statusCode returns 001
		//If choked, interrested and preferreded it returns 111
		//Define behavior with a switch using the various combincations instead of if(true && true || false) etc
	}
	
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
	
	public static void main(String[] args) {
		Neighbor nei = new Neighbor();
		nei.choke();
		nei.disinterested();
		nei.prefer();
		System.out.println(nei.getStatus());
	}
}
