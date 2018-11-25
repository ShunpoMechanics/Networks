
/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class Message {

    /**
     * Define MessageType enum.
     */
    public enum MessageType {
        choke((byte) 0),
        unchoke((byte) 1),
        interested((byte) 2),
        notInterested((byte) 3),
        have((byte) 4),
        bitfield((byte) 5),
        request((byte) 6),
        piece((byte) 7);

        private final byte msgTypeVal;

        private MessageType(byte msgTypeVal) {
            this.msgTypeVal = msgTypeVal;
        }

        public byte getMessageType() {
            return this.msgTypeVal;
        }
    }

    private final int messageLength;
    private final MessageType messageType;
    private final byte[] messagePayload;

    /**
     *
     * @param msgLength
     * @param msgType
     * @param msgPayload
     */
    Message(int msgLength, MessageType msgType, byte[] msgPayload) throws Exception {
        messageLength = msgLength;
        messageType = msgType;
        messagePayload = msgPayload;

        if (!isValid(msgLength, msgType, msgPayload)) {
            Flags.print("Message creation failed..." + toString(), Flags.Debug.ERROR);
            throw new Exception("Message creation failed");
        }
    }

    /**
     * @return the messageLength
     */
    public int getMessageLength() {
        return messageLength;
    }

    /**
     * @return the messageType
     */
    public MessageType getMessageType() {
        return messageType;
    }

    /**
     * @return the messagePayload
     */
    public byte[] getMessagePayload() {
        return messagePayload;
    }

    @Override
    public String toString() {
        return "Length: " + getMessageLength() + ", Type: " + getMessageType() + ", Payload 1st byte: " + (getMessagePayload().length > 0 ? getMessagePayload()[0] : "NONE");
    }

    private boolean isValid(int msgLength, MessageType msgType, byte[] msgPayload) {
        if (msgType == MessageType.choke
                || msgType == MessageType.unchoke
                || msgType == MessageType.interested
                || msgType == MessageType.notInterested) {
            if (messagePayload != null) {
                System.err.println("messagePayload is NOT NULL for a message that MUST NOT have payload");
                return false;
            }
        } else {
            if (messagePayload == null) {
                System.err.println("messagePayload is NULL for a message that MUST have payload");
                return false;
            }
        }

        if (messageLength < 0) {
            System.err.println("messageLength is negative");
            return false;
        }
        if (messageLength != msgPayload.length) {
            System.err.println("messageLength is not equal to length of payload byte array");
            return false;
        }
        return true;
    }

}
