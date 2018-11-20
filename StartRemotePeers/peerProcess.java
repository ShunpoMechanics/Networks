/**
 *
 * @author Kimberly Ying (kimying@ufl.edu)
 */

import java.util.*;
import java.io.*;
import java.lang.*;
import java.net.*;

public class peerProcess implements Runnable {
	static int peerID;
	static String handshakeHeader;
	private final static byte[] zeroBits = new byte[10];
	PeerInfoReader pir;

	// chokedUnchoked key value pair (int peerID, boolean choke)
	// bitfields key value pair (int peerID, bitSet bitfield)

	public void run () {
		Log.writeLog(peerID, " in main run of peerProcess");
		System.out.println(" in peer process run");
		try {
			pir = new PeerInfoReader("../LocalPeerInfo.cfg");
			Log.writeLog(peerID, " pir neighbors created");
			System.out.println(" created peer info reader");
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
			Log.writeLog(peerID, " couldn't find LocalPeerInfo file ");
		} catch (IOException e) {
			e.printStackTrace(System.out);
			Log.writeLog(peerID, " IOException ");
		}
		Log.writeLog(peerID, " after first try/catch ");
		int listentingPort = pir.neighbors.get(peerID).getListeningPort();
		ServerSocket listenSocket = null;
		// first must connect to all other remote peers
		try {
			listenSocket = new ServerSocket(listentingPort);
			// peer acting as client to other peers
			String m;
			for (HashMap.Entry<Integer, Neighbor> entry : pir.neighbors.entrySet()) {
				Log.writeLog(peerID, " in for loop iterating over HashMap");
			    if (entry.getKey() != peerID) { // only try to connect to peers not self
			    	Thread clientThread = new Thread(new PeerAsClient(entry.getValue())); // need new thread for every client
			    	clientThread.start();
			    	m = " Trying to connect to " + entry.getValue().getId();
			    	Log.writeLog(peerID, m);
			    }
			}
			int clientNum = 1;
			long startTime = System.currentTimeMillis(); //start time
			while(true && (System.currentTimeMillis()-startTime)<10000) { // peer's server process - will quit after 10 seconds
				new Handler(listenSocket.accept()).start(); 
				clientNum++;
			}
		} catch (IOException e) {
			e.printStackTrace(System.out);
		} finally {
			try {
				if (listenSocket != null) listenSocket.close();
			} catch (IOException e) {
				e.printStackTrace(System.out);
			}
		}


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

	

	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Must have one argument for peerID.");
			System.exit(0);
		}
		peerID = Integer.parseInt(args[0]);
		System.out.println("in main: " + peerID);

		handshakeHeader = "P2PFILESHARINGPROJ" + new String(zeroBits) + args[0];

		try {
			CommonConfigReader ccr = new CommonConfigReader("../Common.cfg");
		} catch (FileNotFoundException e) {
			e.printStackTrace(System.out);
		} catch (IOException e) {
			e.printStackTrace(System.out);
		}

		// Start thread
		Thread t1 = new Thread(new peerProcess());
		t1.start();

	}

	/**
 	* A handler thread class.  Handlers are spawned from the listening
 	* loop and are responsible for dealing with a single client's requests.
 	*/
	private class Handler extends Thread { // server to client
    	private String message;    //message received from the client
		private String MESSAGE;    //uppercase message send to the client
		private Socket connection;
    	private ObjectInputStream in;	//stream read from the socket
    	private ObjectOutputStream out;    //stream write to the socket
		private int no = 0000;		//The index number of the client

    	public Handler(Socket connection) {
        	this.connection = connection;
    	}

        public void run() {
	 		try{
				//initialize Input and Output streams
				out = new ObjectOutputStream(connection.getOutputStream());
				out.flush();
				in = new ObjectInputStream(connection.getInputStream());
				try{
					// handshake
					System.out.println("Waiting for message from client");
					message = (String)in.readObject();
					System.out.println("got message from client");
					Log.TCPfrom(no, peerID);
					if (message.substring(0, message.length()-4).equals("P2PFILESHARINGPROJ" + new String(zeroBits))) {
						no = Integer.parseInt(message.substring(message.length()-4));
						MESSAGE = handshakeHeader;
						sendMessage(MESSAGE);
						Log.TCPfrom(no, peerID);
						// while not all pieces have complete file
						// could set this while loop on a timer for testing purposes
/*						while (!allHaveCompleteFile(pir.neighbors)) { //this will currently cause infinite loop since we aren't passing files yet
							//receive the message sent from the client
							message = (String)in.readObject();
							//show the message to the user
							System.out.println("Receive message: " + message + " from client " + no);
							//Capitalize all letters in the message
							MESSAGE = message.toUpperCase();
							//send MESSAGE back to the client
							sendMessage(MESSAGE);
						}*/
					}


				} catch(ClassNotFoundException classnot){
					System.err.println("Data received in unknown format");
				}
			} catch(IOException ioException){
				System.out.println("Disconnect with Client " + no);
			}
			finally{
				//Close connections
				try{
					in.close();
					out.close();
					connection.close();
				}
				catch(IOException ioException){
					System.out.println("Disconnect with Client " + no);
				}
			}
		}

		//send a message to the output stream
		void sendMessage(String msg)
		{
			try{
				//stream write the message
				out.writeObject(msg);
				out.flush();
			}
			catch(IOException ioException){
				ioException.printStackTrace();
			}
		}
	}

	public class PeerAsClient implements Runnable { // client to server
		Socket requestSocket;           //socket connect to the server
		ObjectOutputStream out;         //stream write to the socket
	 	ObjectInputStream in;          //stream read from the socket
		String message;                //message send to the server
		String MESSAGE;                //capitalized message read from the server
		String ip;
		int port;
		int serverID;
		String serverHandshakeHeader;

		public PeerAsClient(Neighbor server) { // the ip and port of the peer it wants to connect to
			serverID = server.getId();
			ip = server.getHostname();
			port = server.getListeningPort();
			serverHandshakeHeader = "P2PFILESHARINGPROJ" + new String(zeroBits) + serverID;
		}

		public void run()
		{
			try{
				//create a socket to connect to the server
				requestSocket = new Socket(ip, port);
				System.out.println("Connected to " + ip + " in port " + port);
				//initialize inputStream and outputStream
				out = new ObjectOutputStream(requestSocket.getOutputStream());
				out.flush();
				in = new ObjectInputStream(requestSocket.getInputStream());
				
				//get Input from standard input
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
				System.out.println("Writing message to server");
				// handshake
				message = handshakeHeader;
				sendMessage(message);
				Log.TCPto(peerID, serverID);
				MESSAGE = (String)in.readObject();
				if (MESSAGE.equals(serverHandshakeHeader)) {
					Log.TCPto(peerID, serverID);
					// could set this while loop on a timer for testing purposes
/*					while(true)
					{
						System.out.print("Hello, please input a sentence: ");
						//read a sentence from the standard input
						message = bufferedReader.readLine();
						//Send the sentence to the server
						sendMessage(message);
						//Receive the upperCase sentence from the server
						MESSAGE = (String)in.readObject();
						//show the message to the user
						System.out.println("Receive message: " + MESSAGE);
					}*/
				}
				
			}
			catch (ConnectException e) {
	    			System.err.println("Connection refused. You need to initiate a server first.");
			} 
			catch (ClassNotFoundException e) {
	            		System.err.println("Class not found");
	        	} 
			catch (UnknownHostException unknownHost) {
				System.err.println("You are trying to connect to an unknown host!");
			}
			catch (IOException ioException) {
				ioException.printStackTrace();
			}
			finally{
				//Close connections
				try{
					if (in != null) in.close();
					if (out != null) out.close();
					if (requestSocket != null) requestSocket.close();
				}
				catch (IOException ioException) {
					ioException.printStackTrace();
				}
			}
		}
		//send a message to the output stream
		void sendMessage(String msg)
		{
			try{
				//stream write the message
				out.writeObject(msg);
				out.flush();
			}
			catch (IOException ioException) {
				ioException.printStackTrace();
			}
		}
	}

	// handshake function
	public void handshake() {

	}


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

	public static boolean allHaveCompleteFile(HashMap<Integer, Neighbor> peers) {
		for (Neighbor n : peers.values()) {
			if (!n.hasFile()) return false;
		}
		return true;
	}

	public static void setBitfield() {

	}

	// public static int [] determinePreferredNeighbors() {
	// 	int [] neighbors = new int [numPreferredNeighbors]; // (k)
	// 	// if has complete file, determine peers randomly
	// 	// for all interested neighbors (check from peerInfoVector)
	// 	// calculate download rate and keep track of top k, settle ties randomly

	// 	return neighbors;
	// }

	public static void requestPiece(int id) {
		int piece;
		// find all needed pieces with all pieces that peer id has
		// randomly select piece to request
		// call request message function
	}


}
