import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class HuffmanDecompression {
    int n;

    void readCompressedFile(String file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        int bytes = bufferedInputStream.read();

        int extraBytes = bufferedInputStream.read();
        int len = bufferedInputStream.read();
        int padding = bufferedInputStream.read();
        System.out.println(bytes);
        System.out.println(extraBytes);
        System.out.println(len);
        System.out.println(padding);

        byte[] bytesToRead = new byte[5 * 1024];
        StringBuilder nByteGroup = new StringBuilder();
        int bytesReadSoFar;
        int zeros = 0;
        while ((bytesReadSoFar = bufferedInputStream.read(bytesToRead)) != -1) {
            if (bufferedInputStream.available() == 1) {
                zeros = bufferedInputStream.read();
            } else if (bufferedInputStream.available() == 0) {
                zeros = (int) bytesToRead[bytesReadSoFar-1];
            }
        }
    }

    void buildTree() {

    }

    void decodeData() {

    }

    public static void main(String[] args) throws IOException {
        HuffmanDecompression huffmanDecompression = new HuffmanDecompression();
        huffmanDecompression.readCompressedFile("output.txt.hc");
    }
}
