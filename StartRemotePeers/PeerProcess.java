
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
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

        // Start the peer process.
        PeerProcess pp = new PeerProcess(Integer.parseInt(args[0]));
        Thread t = new Thread(pp);
        t.start();
    }

    public volatile boolean isAlive;
    private final int pid;

    public PeerProcess(int pid) throws IOException {
        this.pid = pid;
        // Get the current peer's information from PeerInfoReader.
        Peer current_peer = PeerInfoReader.PEERS.get(pid);
        // Check validity of input pid.
        if (current_peer == null) {
            System.out.println("Peer information not found: " + current_peer);
            System.exit(1);
        }
        isAlive = true;
    }

    @Override
    public void run() {
        // The client part of the peer process:
        // Connect to other PEERS with lower peerID per the project specification.
        CopyOnWriteArrayList<Connection> conns = connectToOthers();
        // Perform handshake.
        for (Connection c : conns) {
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

        // The server part of the peer process:
        Server server = new Server(pid, conns);
        Thread serverThread = new Thread(server);
        serverThread.start();

        // While peer is alive, exchange pieces.
        while (isAlive) {
            for (Connection c : conns) {
                // If connection is still in handshake stage, check the input.
                if (c.status == Connection.Status.HANDSHAKE) {
                    try {
                        Object response = c.in.readObject();
                        if (response != null) {
                            HandshakeMessage.verifyHandshakeMessage(response, c);
                            // If verification was successful, change status to ESTABLISHED and set the pid for the connection.
                            c.status = Connection.Status.ESTABLISHED;
                            c.pid = ((HandshakeMessage) response).pid;
                            // Send bitfield.
                            // TODO.
                        }
                    } catch (Exception e) {
                        Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, e);
                    }
                } else { // Connection is established.
                    int neighbor_pid = c.pid;
                    // Exchange pieces.
                    // TODO.

                }
            }
        }
    }

    private CopyOnWriteArrayList<Connection> connectToOthers() {
        // A thread-safe variant of ArrayList, which is very efficient if the number of mutations is low like this program.
        CopyOnWriteArrayList<Connection> conns = new CopyOnWriteArrayList<>();
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
                    conns.add(conn);
                } catch (IOException ex) {
                    System.out.println("Socket creation to " + neighbor.hostname + ":" + neighbor.listeningPort + " failed");
                    Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return conns;
    }

    class Server implements Runnable {

        private ServerSocket listener;
        private final CopyOnWriteArrayList<Connection> conns;

        public Server(int pid, CopyOnWriteArrayList<Connection> conns) {
            try {
                // Start this peer's listening server.
                listener = new ServerSocket(PeerInfoReader.PEERS.get(pid).listeningPort);
            } catch (IOException ex) {
                System.out.println("Failed to listen on " + PeerInfoReader.PEERS.get(pid).listeningPort);
                Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            // Keep a reference to the connections.
            this.conns = conns;
        }

        @Override
        public void run() {
            // Handle incoming connection requests in this thread.
            try {
                // While the PeerProcess is alive.
                while (isAlive) {
                    Socket s = listener.accept();
                    Connection conn = new Connection(s,
                            new ObjectOutputStream(s.getOutputStream()),
                            new ObjectInputStream(s.getInputStream()));
                    // Keep track of connected PEERS.
                    conns.add(conn);
                }
            } catch (IOException ex) {
                Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
            } finally { // Cleanup.
                try {
                    listener.close();
                } catch (IOException ex) {
                    Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

}
