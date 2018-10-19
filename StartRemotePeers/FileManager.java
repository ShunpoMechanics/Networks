import java.io.File;

public class FileManager {

    public static void split(File file) { //Maybe pass in file size?
        int count = 0;
        int maxSize = 2048; //Get max file size
        int size = 1024; //Change this to read the config file
        byte[] part = new byte[size];
        int parts = maxSize/size; //Calculate number of parts

        String filename = file.getName();
        String newFilename = filename + count;
        for(count = count; count < parts; count++)
        {   for(int i = 0; i < size; i++)
            {
                //Add to part
            }
            //output part then loop
        }
    }

    public static void merge(byte[][] parts, int count){
        File file = new File("filename");

        for(int i = 0; i < count; i++)
        {
            //Write byte array to file then move to next array
        }

    }




}
