import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HuffmanDecompression {
    int bitNo = 0;
    class CharNode {
        String node;
        CharNode leftNode = null;
        CharNode rightNode = null;

        public CharNode(String node) {
            this.node = node;
        }
        public boolean isLeaf() {
            return (this.leftNode == null && this.rightNode == null);
        }
    }
    void readCompressedFile(String file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        int bytes = bufferedInputStream.read();

        int extraBytes = bufferedInputStream.read();
        byte[] treeLen = new byte[4];
        bufferedInputStream.read(treeLen);

        int len = ByteBuffer.wrap(treeLen).getInt();
        int padding = bufferedInputStream.read();
        System.out.println(bytes);
        System.out.println(extraBytes);
        System.out.println(len);
        System.out.println(padding);

        byte[] bytesToRead = new byte[5 * 1024];
        StringBuilder nByteGroup = new StringBuilder();
        int bytesReadSoFar;
        buildTree(bufferedInputStream, len, padding, bytes);
    /*
    // data
        int zeros = 0;
        while ((bytesReadSoFar = bufferedInputStream.read(bytesToRead)) != -1) {
            if (bufferedInputStream.available() == 1) {
                zeros = bufferedInputStream.read();
            } else if (bufferedInputStream.available() == 0) {
                zeros = (int) bytesToRead[bytesReadSoFar-1];
            }
        }
        System.out.println(zeros);

     */
    }

    CharNode traverseTree(StringBuilder tree, int n) {
        System.out.println(bitNo);
     //   0010110011110110111100101110010010111000010111001101001000000101101000101100101
        if(tree.charAt(bitNo) == '1') { // leaf node
            if(bitNo+1+(8*n) <= tree.length()) {
                CharNode node = new CharNode(tree.substring(bitNo + 1, bitNo + 1 + (8*n)));
                bitNo += (1 + (8*n));
                return node;
            }
            else {
                CharNode node = new CharNode(tree.substring(bitNo + 1, tree.length()));
                System.out.println("hena");
                bitNo += (tree.length() - (bitNo + 1));
                return node;
            }
        }
        else {
            bitNo++;
            CharNode leftNode = traverseTree(tree, n);
            CharNode rightNode = traverseTree(tree, n);
            CharNode newNode = new CharNode(null);
            newNode.leftNode = leftNode;
            newNode.rightNode = rightNode;
            return newNode;
        }
    }
    void buildTree(BufferedInputStream bufferedInputStream, int treeSize, int treePadding, int n) throws IOException {
        System.out.println(treeSize);
        System.out.println("Tree size: " + treeSize);
        StringBuilder treeBuilder = new StringBuilder();

        byte[] tree = new byte[treeSize];
        int bytesReadSoFar = 0;
        int b;
        while((b = bufferedInputStream.read()) != -1) {
            bytesReadSoFar++;
            StringBuilder temp = new StringBuilder();
            String s = Integer.toBinaryString((char) b);
            int len = s.length();
            while (len < 8) {
                temp.append('0');
                len++;
            }
            temp.append(s);
            treeBuilder.append(temp);
            System.out.println(temp);
            if(bytesReadSoFar == treeSize) break;

        }

        System.out.println("char: " + treeBuilder.charAt(1));

        System.out.println(treeBuilder.toString().toCharArray().length);

        treeBuilder.delete(treeBuilder.length() - treePadding, treeBuilder.length());
        System.out.println(treeBuilder.length());
        CharNode root = traverseTree(treeBuilder, n);
        System.out.println("Root: " + root.node + " left: " + root.leftNode.node + " right: " + root.rightNode.node);
        System.out.println(root.isLeaf());
        decodeData(root, bufferedInputStream);
    }

    void decodeData(CharNode root, BufferedInputStream bufferedInputStream) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("result.txt");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        CharNode currentNode = root;
        int b;
        System.out.println("hi");
        while((b = bufferedInputStream.read()) != -1) {
            StringBuilder temp = new StringBuilder();
            String s = Integer.toBinaryString((char) b);
            int len = s.length();
            System.out.println("s: " + s);
            while (len < 8) {
                temp.append('0');
                len++;
            }
            temp.append(s);
            System.out.println("tem: " + temp.toString());
            for(int i = 0; i<temp.length(); i++) {
            //    System.out.println("d5lt");
                if(currentNode.isLeaf()) {
                    String decodedData = currentNode.node;
                    System.out.println("de " + decodedData);
                    for(int i1=0; i1<decodedData.length(); i1++) {
                        bufferedOutputStream.write((int) (char) decodedData.charAt(i1));
                    }
                    currentNode = root;
                }
                if(temp.charAt(i) == '0') {
                    currentNode = currentNode.leftNode;
                }
                else currentNode = currentNode.rightNode;
            }
        }
        bufferedOutputStream.close();

    }

    public static void main(String[] args) throws IOException {
        HuffmanDecompression huffmanDecompression = new HuffmanDecompression();
        huffmanDecompression.readCompressedFile("output.txt.hc");
    }
}
