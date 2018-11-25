import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.*;
import static java.lang.Math.pow;

import static java.lang.Math.toIntExact;
//import CommonConfigReader;

public class FileManager {

    static CommonConfigReader ccr;
    static int pieceSize;
    static int numPieces;
    static String extension = "";
    private static List<File> pieces = new ArrayList<>();
    {
        try {
            ccr = new CommonConfigReader("testCommon.cfg");
            pieceSize = toIntExact(ccr.pieceSize);
            numPieces = toIntExact(ccr.fileSize/pieceSize);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void split(File file) throws IOException {
        int count = 0;

        byte[] part = new byte[pieceSize];

        String filename = file.getName();

        FileInputStream input = new FileInputStream(file);
        BufferedInputStream buffer = new BufferedInputStream(input);

        int bufferSize = 0;

        while((bufferSize = buffer.read(part)) > 0)
        {
            String filePart = filename + count; //Make part name
            File outputFile = new File(file.getParent(), filePart); //Create output file

            FileOutputStream out = new FileOutputStream(outputFile); //Create output stream

            out.write(part, 0, bufferSize); //Actually output file
            count++;
        }
    }

    public static void merge(List<File> parts) throws IOException {
        String temp = "";
        //This loop and the next are for testing
        //Get extension of the file
        for(int i = ccr.fileName.length() - 1; i > 0; i--)
        {
            char index = ccr.fileName.charAt(i);
            if(index == '.')
                break;
            else
                temp += index;
        }

        //Reverse extension
        for(int i = temp.length() - 1; i > -1; i--)
        {
            extension += temp.charAt(i);
        }

        File outputFile = new File("out." + extension);
        //File outputFile = new File(ccr.fileName);

        byte[] part = new byte[pieceSize];
        FileOutputStream out = new FileOutputStream((outputFile),true);
        int bufferSize;

        for(int i = 0; i < numPieces + 1; i++)
        {
            FileInputStream input = new FileInputStream(parts.get(i));
            BufferedInputStream buffer = new BufferedInputStream(input);
            bufferSize = buffer.read(part);
            out.write(part, 0, bufferSize);
        }
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
        try{
            while((bufferSize = fis.read(buffer)) > 0)
            {
                byteOutput.write(buffer, 0, bufferSize);
            }
        } catch(IOException e){
            System.out.println(e.getStackTrace());
        }

        outputStream.write(byteOutput.toByteArray());
        outputStream.flush();

    }
	
	public byte[] sendPiece(String pathname) throws IOException { //Functionality without sockets
		File file = new File(pathname); //Get the correct file part
        FileInputStream fis = new FileInputStream(file);
        ByteArrayOutputStream byteOutput = new ByteArrayOutputStream();
        byte[] buffer = new byte[pieceSize];

        int bufferSize = 0;
        //Write ByteArrayOutputStream
        try{
            while((bufferSize = fis.read(buffer)) > 0)
            {
                byteOutput.write(buffer, 0, bufferSize);
            }
        } catch(IOException e){
            System.out.println(e.getStackTrace());
        }

        byte[] output = byteOutput.toByteArray();
		return output;
        
	}
	
	public void receivePiece(byte[] input, String pathname) throws IOException { //Functionality without sockets
		File file = new File(pathname);
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(input);
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

    public static void pieceGatherer() {
        for(int i = 0; i < numPieces + 1; i++)
        {
            pieces.add(new File(ccr.fileName + i));
        }
    }
	
	public static double timer() {
			double time = System.nanoTime();
			time = time/(pow(10, 9));
			return time;
	}

    public static void main(String[] args) throws IOException {
        /*File file2 = new File(".");
        for(String fileNames : file2.list()) System.out.println(fileNames);
        */
        FileManager fm = new FileManager();
        File file = new File(ccr.fileName);
        double start = fm.timer();
        split(file);
        double stop = fm.timer();

        System.out.println("Splitting took " + (stop-start) + " seconds.");
        //Use this to collect all the pieces
        pieceGatherer();

        start = fm.timer();
        //Merge the pieces
        merge(pieces);
        stop = fm.timer();

        System.out.println("Merging took " + (stop-start) + " seconds.");
    }
}
