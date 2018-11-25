
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class Connection {

    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    Status status;
    // The pid of the other side of this connection, defaults to -1 until handshake message is received.
    int pid;

    public enum Status {
        HANDSHAKE,
        ESTABLISHED
    }

    public Connection(Socket socket, ObjectOutputStream out, ObjectInputStream in) {
        this.socket = socket;
        this.out = out;
        this.in = in;
        status = Status.HANDSHAKE;
        pid = -1;
    }

}
