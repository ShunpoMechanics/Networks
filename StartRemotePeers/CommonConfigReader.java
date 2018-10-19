
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

/**
 *
 * @author Tima Tavassoli (ftavassoli@ufl.edu)
 */
public class CommonConfigReader {

//    A simple main to test this class.
    public static void main(String args[]) throws IOException {
        CommonConfigReader ss = new CommonConfigReader("Common.cfg");
        Flags.print("Test main of CommonConfigReader finished", Flags.Debug.INFO);
    }

    final int numberOfPreferredNeighbors;
    final int unchokingInterval;
    final int optimisticUnchokingInterval;
    final String fileName;
    final long fileSize;
    final long pieceSize;

    /**
     * Reads the Common.cfg file.
     * @param configFilePath
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public CommonConfigReader(String configFilePath) throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader(configFilePath));
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
        pieceSize = sc.nextLong();

        Flags.print("Finished reading " + configFilePath, Flags.Debug.INFO);
        Flags.print(this.toString(), Flags.Debug.INFO);
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
