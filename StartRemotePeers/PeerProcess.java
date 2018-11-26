
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class PeerProcess implements Runnable {

    // Get a reference to Common.cfg's Singleton object.
    private final CommonConfigReader commonConfig = CommonConfigReader.getInstance();

    /**
     * PeerProcess is the entry point of this program.
     *
     * @param args
     *
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
    private final int local_pid;

    public PeerProcess(int local_pid) throws IOException {
        this.local_pid = local_pid;
        // Get the current peer's information from PeerInfoReader.
        Peer current_peer = PeerInfoReader.PEERS.get(local_pid);
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
        CopyOnWriteArrayList<Connection> conns = PeerUtils.connectToOthers(local_pid);
        // Create a thread to handle the exchanges.
        for (Connection conn : conns) {
            // Handle exchanges in a separate thread.
            new Thread(new ConnectionHandler(conn)).start();
        }

        // The server part of the peer process:
        Server server = new Server(local_pid, conns);
        Thread serverThread = new Thread(server);
        serverThread.start();
    }

    class Server implements Runnable {

        private ServerSocket listener;
        private final CopyOnWriteArrayList<Connection> conns;
        private int local_pid;

        public Server(int local_pid, CopyOnWriteArrayList<Connection> conns) {
            this.local_pid = local_pid;
            // Keep a reference to the connections.
            this.conns = conns;
        }

        @Override
        public void run() {
            // Handle incoming connection requests in this thread.
            try {
                // Start this peer's listening server.
                listener = new ServerSocket(PeerInfoReader.PEERS.get(this.local_pid).listeningPort);
                System.out.println("ServerSocket for local_pid " + this.local_pid + " started on port " + listener.getLocalPort());
            } catch (IOException ex) {
                System.out.println("Failed to listen on " + PeerInfoReader.PEERS.get(this.local_pid).listeningPort);
                Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(1);
            }
            try {
                // While the PeerProcess is alive.
                while (isAlive) {
                    Socket s = listener.accept();
                    Connection conn = new Connection(s,
                            new ObjectOutputStream(s.getOutputStream()),
                            new ObjectInputStream(s.getInputStream()),
                            this.local_pid);
                    // Keep track of connected PEERS.
                    conns.add(conn);
                    // Create a new connection handler.
                    new Thread(new ConnectionHandler(conn)).start();
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

    class ConnectionHandler implements Runnable {

        Connection conn;

        public ConnectionHandler(Connection conn) {
            this.conn = conn;
        }

        @Override
        public void run() {
            // Send handshake.
            try {
                // Send a HandshakeMessage with pid set to the current peer's pid.
                System.out.println("pid " + local_pid + ": sending handshake to " + conn.socket.getInetAddress() + ":" + conn.socket.getPort());
                conn.out.writeObject(new HandshakeMessage(local_pid));
                conn.out.flush();
            } catch (IOException ex) {
                System.err.println("ObjectOutputStream to " + conn.socket.getInetAddress() + ":" + conn.socket.getPort() + " failed");
                Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, ex);
            }
            // While peer is alive, exchange pieces.
            while (isAlive) {
                try {
                    // If connection is still in handshake stage, check the input.
                    if (conn.status == Connection.Status.HANDSHAKE) {
                        Object response = conn.in.readObject();
                        if (response != null) {
                            HandshakeMessage.verifyHandshakeMessage(response, conn);
                            // If verification was successful, change status to ESTABLISHED and set the remote_pid for the connection.
                            conn.status = Connection.Status.ESTABLISHED;
                            conn.remote_pid = ((HandshakeMessage) response).pid;
                            System.out.println("pid " + conn.local_pid + ": HandshakeMessage successfully received from " + conn.remote_pid);
                            // Send the bitfield message of the current peer to other peer.
                            Peer current = PeerInfoReader.PEERS.get(conn.local_pid);
                            byte[] bitfield = current.bitfield.toByteArray();
                            conn.out.writeObject(new Message(bitfield.length, Message.MessageType.bitfield, bitfield));
                            conn.out.flush();
                        } else {
                            System.err.println("Null response");
                        }
                    } else { // Connection is established.
                        Peer current = PeerInfoReader.PEERS.get(conn.local_pid);
                        Peer peer = PeerInfoReader.PEERS.get(conn.remote_pid);
                        // Exchange pieces with neighbor.
                        // Check the input stream of connection to read the arrived messages.
                        Message response = (Message) conn.in.readObject();
                        switch (response.getMessageType()) {
                            case choke: {
                                peer.peerChokedCurrentClient = true;
                                break;
                            }
                            case unchoke: {
                                peer.peerChokedCurrentClient = false;
                                // Send request to this peer if current client is interested in the peer
                                // and the peer doesn't have all the pieces.
                                if (peer.currentClientInterestedInPeer
                                        && current.bitfield.cardinality() < commonConfig.numPieces) {
                                    Message req = PeerUtils.generateRequestMessageFrom(conn.local_pid);
                                    conn.out.writeObject(req);
                                    conn.out.flush();
                                }
                                break;
                            }
                            case interested: {
                                peer.peerInterestedInCurrentClient = true;
                                // TODO: Anything else here?
                                break;
                            }
                            case notInterested: {
                                peer.peerInterestedInCurrentClient = false;
                                // TODO: Anything else here?
                                break;
                            }
                            case have: {
                                // If have was received, update bitfield of remote peer.
                                byte[] bytes = response.getMessagePayload();
                                // Payload must be 4 bytes.
                                if (bytes.length != 4) {
                                    System.err.println("Received 'have' message has payload of size " + bytes.length);
                                    break;
                                }
                                int pieceIndex = ByteBuffer.wrap(bytes).getInt();
                                peer.bitfield.set(pieceIndex);
                                // Determine whether current client should send an ‘interested’ message.
                                if (!current.bitfield.get(pieceIndex)) { // If current client doesn't have this piece.
                                    Message interested = PeerUtils.generateInterestMessageTo(peer);
                                    conn.out.writeObject(interested);
                                    conn.out.flush();
                                } else {
                                    // If current client already has the piece, and 
                                    // the neighbor doesn't have any interesting pieces (i.e. the neighbor's bitfield is a subset of our bitfield), 
                                    // send `notInterested`.
                                    BitSet tmp = (BitSet) current.bitfield.clone();
                                    tmp.and(peer.bitfield);
                                    // If the result of `and`, is the same as the other peer's bitfield, then other bitfield is a subset of current client's bitfield.
                                    if (tmp.equals(peer.bitfield)) {
                                        Message not_interested = PeerUtils.generateNotInterestMessageTo(peer);
                                        conn.out.writeObject(not_interested);
                                        conn.out.flush();
                                    }
                                }
                                break;
                            }
                            case bitfield: {
                                // If bitfield was received, set bitfield of remote peer.
                                peer.bitfield = BitSet.valueOf(response.getMessagePayload());
                                // Determine whether current client should send an ‘interested’ message.
                                BitSet tmp = (BitSet) current.bitfield.clone();
                                tmp.and(peer.bitfield);
                                // If the result of `and`, is the same as the other peer's bitfield, then other bitfield is a subset of current client's bitfield.
                                if (tmp.equals(peer.bitfield)) {
                                    Message not_interested = PeerUtils.generateNotInterestMessageTo(peer);
                                    conn.out.writeObject(not_interested);
                                    conn.out.flush();
                                } else { // Else, peer has something that current client doesn't, send `interested`.
                                    Message interested = PeerUtils.generateInterestMessageTo(peer);
                                    conn.out.writeObject(interested);
                                    conn.out.flush();
                                }
                                // TODO: Anything else here?
                                break;
                            }
                            case request: {
                                break;
                            }
                            case piece: {
                                // If a piece was received, update bitfield of current client.

                                break;
                            }
                        }
                        // TODO.
                        // Write the appropriate output.
                        // TODO.
                    }
                } catch (Exception e) {
                    Logger.getLogger(PeerProcess.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }

    }

}
