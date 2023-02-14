import java.io.File;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException {
	// write your code here
        String action = args[0];
        String path = args[1];
        File fileInput = new File(path);
        if(action.charAt(0) == 'c') {
            int n = Integer.parseInt(args[2]);
            HuffmanCompression compression = new HuffmanCompression();
            String resultPath = (fileInput.getParent() != null) ?
                    fileInput.getParent() + "/19016871" + "." + args[2] + "." + fileInput.getName() + "." + "hc" :
                    "19016871" + "." + args[2] + "." + fileInput.getName() + "." + "hc";
            long start = System.currentTimeMillis();
            compression.compressionAlgorithm(path, n, resultPath);
            long end = System.currentTimeMillis();
            File fileOutput = new File(resultPath);
            System.out.println("Time in ms: " + (end - start) + "   Time in s: " + ((end - start)/1000));
            System.out.println("Compression Ratio: " + fileInput.length() / fileOutput.length());
        }

        else if(action.charAt(0) == 'd') {
            HuffmanDecompression decompression = new HuffmanDecompression();
            int index = path.lastIndexOf('.');
            String resultPath = (fileInput.getParent() != null) ?
                    fileInput.getParent() + "/extracted" + "." + path.substring(0, index) :
                    "/extracted" + "." + path.substring(0, index);
            long start = System.currentTimeMillis();
            decompression.readCompressedFile(path, resultPath);
            long end = System.currentTimeMillis();
            System.out.println("Time in ms: " + (end - start) + "   Time in s: " + ((end - start)/1000));
        }

    }
}
