
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class CommonConfigReader {

    // Path to Common.cfg file.
    static final String CONFIG_FILE_PATH = "Common.cfg";
    private static CommonConfigReader instance;

    public static CommonConfigReader getInstance() {
        if (instance == null) {
            try {
                instance = new CommonConfigReader();
            } catch (IOException ex) {
                Logger.getLogger(CommonConfigReader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return instance;
    }

    final int numberOfPreferredNeighbors;
    final int unchokingInterval; // in seconds.
    final int optimisticUnchokingInterval; // in seconds.
    final String fileName;
    final long fileSize;
    final int pieceSize;
    final int numPieces;

    /**
     * Reads the Common.cfg file.
     *
     * @throws FileNotFoundException
     * @throws IOException
     */
    private CommonConfigReader() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(CONFIG_FILE_PATH));
        Scanner sc = new Scanner(br);
        // For each of the 6 parameters, ignore the name of the parameter, then read the parameter.
        sc.next();
        numberOfPreferredNeighbors = sc.nextInt();
        sc.next();
        unchokingInterval = sc.nextInt();
        sc.next();
        optimisticUnchokingInterval = sc.nextInt();
        sc.next();
        fileName = sc.next();
        sc.next();
        fileSize = sc.nextLong();
        sc.next();
        pieceSize = sc.nextInt();

        // Calculate the number of pieces.
        numPieces = (int) Math.ceil((double) fileSize / pieceSize);

        System.out.println("Finished reading " + CONFIG_FILE_PATH);
    }

    @Override
    public final String toString() {
        return "    numberOfPreferredNeighbors: " + numberOfPreferredNeighbors + "\n"
                + "    unchokingInterval: " + unchokingInterval + "\n"
                + "    optimisticUnchokingInterval: " + optimisticUnchokingInterval + "\n"
                + "    fileName: " + fileName + "\n"
                + "    fileSize: " + fileSize + "\n"
                + "    pieceSize: " + pieceSize;
    }

}
