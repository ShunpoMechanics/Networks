
/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class HandshakeMessage {

    // 36 bytes instead of 18, because Java uses UTF-16 coding.
    final String header = "P2PFILESHARINGPROJ";
    // 10 bytes of zeros.
    final byte[] zeros = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
    // peer ID;
    int pid;

    public HandshakeMessage(int pid) {
        this.pid = pid;
    }

}
