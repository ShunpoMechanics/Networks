
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class PeerProcess implements Runnable {

    /**
     * PeerProcess is the entry point of this program.
     *
     * @param args
     * @throws IOException
     */
    public static void main(String args[]) throws IOException {
        // Make sure the input has a peer ID argument.
        if (args.length < 1 || args[0].isEmpty() || !Character.isDigit(args[0].charAt(0))) {
            System.err.println("Invalid input args, please pass a peerID.");
            System.exit(1);
        }

        // Read and parse PeerInfo.cfg into PeerInfoReader.PEERS.
        PeerInfoReader pir = new PeerInfoReader("PeerInfo.cfg");
        if (PeerInfoReader.PEERS.isEmpty()) {
            System.err.println("PEERS mapping is empty, PeerInfoReader failed.");
            System.exit(1);
        }
        
        // Check validity of input pid.
        int pid = Integer.parseInt(args[0]);
        if (!PeerInfoReader.PEERS.containsKey(pid)) {
            System.err.println("The provided peerID does not exist in the PeerInfo.cfg file.");
            System.exit(1);
        }

        // Start the peer process.
        PeerProcess pp = new PeerProcess(pid);
        Thread t = new Thread(pp);
        t.start();
    }

    public volatile boolean isAlive;
    private ServerSocket listener;
    private final int pid;

    public PeerProcess(int pid) throws IOException {
        this.pid = pid;
        // Get the current peer's information from PeerInfoReader.
        Peer current_peer = PeerInfoReader.PEERS.get(pid);
        if (current_peer == null) {
            System.out.println("Peer information not found: " + current_peer);
            System.exit(1);
        }
        isAlive = true;
    }

    @Override
    public void run() {
        // The server part of the peer process:
        try {
            // Start this peer's listening server.
            listener = new ServerSocket(PeerInfoReader.PEERS.get(pid).listeningPort);
        } catch (IOException ex) {
            System.out.println("Failed to listen on " + PeerInfoReader.PEERS.get(pid).listeningPort);
            Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
            System.exit(1);
        }
        // Handle incoming requests in a separate thread.
        // TODO.

        // The client part of the peer process:
        // Connect to other PEERS.
        HashMap<Integer, Connection> pid_2_conn = connectToOthers();
        // Perform handshake.
        for (Integer id : pid_2_conn.keySet()) {
            Connection c = pid_2_conn.get(id);
            try {
                // Send a HandshakeMessage with pid set to the current peer's pid.
                System.out.println("pid " + pid + " sending handshake to " + c.socket.getInetAddress() + ":" + c.socket.getPort());
                c.out.writeObject(new HandshakeMessage(pid));
                c.out.flush();
            } catch (IOException ex) {
                System.out.println("ObjectOutputStream to " + c.socket.getInetAddress() + ":" + c.socket.getPort() + " failed");
                Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // While peer is alive, exchange pieces.
        while (isAlive) {
            for (Integer id : pid_2_conn.keySet()) {
                Connection c = pid_2_conn.get(id);
                // If connection is still in handshake stage, check the input.
                if (c.status == Connection.Status.HANDSHAKE) {
                    try {
                        Object response = c.in.readObject();
                        if (response != null) {
                            verifyHandshakeMessage(response, id);
                            c.status = Connection.Status.ESTABLISHED;
                            // Send bitfield.
                            // TODO.
                        }
                    } catch (Exception e) {
                        Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, e);
                    }
                } else { // Connection is established.
                    // Exchange pieces.
                }
            }
        }
    }

    private HashMap<Integer, Connection> connectToOthers() {
        HashMap<Integer, Connection> pid_2_conn = new HashMap<>();
        // Connect to other PEERS with pid lower than this peer's pid.
        for (Integer id : PeerInfoReader.PEERS.keySet()) {
            if (id < pid) {
                Peer neighbor = PeerInfoReader.PEERS.get(id);
                try {
                    Socket s = new Socket(neighbor.hostname, neighbor.listeningPort);
                    Connection conn = new Connection(s,
                            new ObjectOutputStream(s.getOutputStream()),
                            new ObjectInputStream(s.getInputStream()));
                    // Keep track of connected PEERS.
                    pid_2_conn.put(id, conn);
                } catch (IOException ex) {
                    System.out.println("Socket creation to " + neighbor.hostname + ":" + neighbor.listeningPort + " failed");
                    Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return pid_2_conn;
    }

    private void verifyHandshakeMessage(Object obj, int id) {
        try {
            HandshakeMessage msg = (HandshakeMessage) obj;
            if (!msg.header.equals("P2PFILESHARINGPROJ")) {
                throw new Exception("Wrong header");
            } else if (msg.pid != id) {
                throw new Exception("Wrong pid");
            }
        } catch (Exception e) {
            System.out.println("Could not verify HandshakeMessage");
            System.exit(1);
        }
    }

}
