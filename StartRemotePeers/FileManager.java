
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import static java.lang.Math.pow;

import static java.lang.Math.toIntExact;
import java.nio.ByteBuffer;
//import CommonConfigReader;

public class FileManager {

    static CommonConfigReader ccr = CommonConfigReader.getInstance();
    static int pieceSize = ccr.pieceSize;
    static int numPieces = ccr.numPieces;
    static String extension = "";
    private static List<File> pieces = new ArrayList<>();

    public static void split(File file) throws IOException {
        int count = 0;

        byte[] part = new byte[pieceSize];

        String filename = file.getName();

        FileInputStream input = new FileInputStream(file);
        BufferedInputStream buffer = new BufferedInputStream(input);

        int bufferSize = 0;

        while ((bufferSize = buffer.read(part)) > 0) {
            String filePart = filename + count; //Make part name
            File outputFile = new File(file.getParent(), filePart); //Create output file

            FileOutputStream out = new FileOutputStream(outputFile); //Create output stream

            out.write(part, 0, bufferSize); //Actually output file
            out.flush();
            out.close();
            count++;
        }
    }

    public static void merge(int pid, List<File> parts) throws IOException {
        File outputFile = new File(System.getProperty("user.dir") + "/peer_" + pid + "/" + ccr.fileName);

        byte[] part = new byte[pieceSize];
        FileOutputStream out = new FileOutputStream((outputFile));
        int bufferSize;

        for (int i = 0; i < numPieces; i++) {
            BufferedInputStream bis = new BufferedInputStream(new FileInputStream(parts.get(i)));
            bufferSize = bis.read(part);
            out.write(part, 0, bufferSize);
            bis.close();
        }
        out.flush();
        out.close();
    }

    public void sendPiece(Socket socket, String pathname) throws IOException {

        OutputStream outputStream = socket.getOutputStream();
        //Convert file into Bytes to send
        File file = new File(pathname); //Get the correct file part
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        byte[] buffer = new byte[pieceSize];

        int bufferSize = 0;
        //Write ByteArrayOutputStream
        try {
            while ((bufferSize = fis.read(buffer)) > 0) {
                byteOutput.write(buffer, 0, bufferSize);
            }
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }

        outputStream.write(byteOutput.toByteArray());
        outputStream.flush();

    }

    public byte[] sendPiece(String pathname, int pieceIndex) throws IOException { //Functionality without sockets
        File file = new File(pathname); //Get the correct file part
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        byte[] buffer = new byte[pieceSize];
        // First four bytes are the piece index, write them first.
        byte[] pieceIndexBytes = ByteBuffer.allocate(4).putInt(pieceIndex).array();
        byteOutput.write(pieceIndexBytes);

        int bufferSize = 0;
        //Write ByteArrayOutputStream
        try {
            while ((bufferSize = fis.read(buffer)) > 0) {
                byteOutput.write(buffer, 0, bufferSize);
            }
        } catch (IOException e) {
            System.out.println(e.getStackTrace());
        }
        byteOutput.flush();
        byte[] output = byteOutput.toByteArray();
        fis.close();

        return output;

    }

    public void receivePiece(byte[] input, String pathname) throws IOException { //Functionality without sockets
        File file = new File(pathname);
        FileOutputStream fos = new FileOutputStream(file);
        // Start at byte 4, because bytes 0-3 are the piece index.
        fos.write(input, 4, input.length - 4);
        fos.flush();
        fos.close();

    }

    public void receivePiece(ServerSocket serverSocket, String pathname) throws IOException {
        Socket socket = serverSocket.accept();
        InputStream inputStream = socket.getInputStream();

        byte[] fileBytes = new byte[pieceSize];

        inputStream.read(fileBytes);

        File file = new File(pathname);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(fileBytes);
        fos.flush();
        fos.close();
    }

    public static List<File> pieceGatherer(int pid) {
        for (int i = 0; i < numPieces; i++) {
            pieces.add(new File(System.getProperty("user.dir") + "/" + "peer_" + pid + "/" + ccr.fileName + i));
        }
        return pieces;
    }

    public static void removePieces(List<File> pieces) {
        for (File file : pieces) {
            if (!file.isDirectory()) {
                file.delete();
            }
        }
    }
}
