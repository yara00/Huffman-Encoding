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
        System.out.println(bytes);
    }

    public static void main(String[] args) throws IOException {
        HuffmanDecompression huffmanDecompression = new HuffmanDecompression();
        huffmanDecompression.readCompressedFile("output.txt.hc");
    }
}
