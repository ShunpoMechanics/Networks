
import java.io.IOException;
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
    int local_pid;
    // The remote_pid of the other side of this connection, defaults to -1 until handshake message is received.
    int remote_pid;

    public enum Status {
        HANDSHAKE,
        ESTABLISHED
    }

    public Connection(Socket socket, ObjectOutputStream out, ObjectInputStream in, int local_pid) {
        this.socket = socket;
        this.out = out;
        this.in = in;
        this.local_pid = local_pid;
        status = Status.HANDSHAKE;
        remote_pid = -1;
    }

    /**
     * Synchronized method that writes and flushes the given object to this
     * connection's ObjectOutputStream.
     *
     * @param obj
     * @throws IOException
     */
    public synchronized void writeAndFlush(Object obj) throws IOException {
        if (obj != null) {
            this.out.writeObject(obj);
            this.out.flush();
        }
    }

}
