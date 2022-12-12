import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HuffmanDecompression {
    int bitNo = 0;
    StringBuilder treeBuilder = new StringBuilder();

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

    CharNode traverseTree(int n) {
       // bitNo++;
        System.out.println("bit no abl: " + bitNo);
        System.out.println("tree len: " + treeBuilder.length());
        if(treeBuilder.charAt(bitNo) == '1') {// leaf node
            CharNode node = new CharNode(treeBuilder.substring(bitNo + 1, bitNo + 1 + (8*n)));
            System.out.println("leaf data: " + node.node);
            //System.out.println(node.node.toCharArray().length);
            bitNo += (1+(8*n));
            return node;
        }
        bitNo++;
        System.out.println("bit no b3d: " +bitNo );

        CharNode newNode = new CharNode(null);
        newNode.leftNode = traverseTree(n);
        newNode.rightNode = traverseTree(n);
         //   newNode.leftNode = leftNode;
          //  newNode.rightNode = rightNode;
        return newNode;

    }
    void buildTree(BufferedInputStream bufferedInputStream, int treeSize, int treePadding, int n) throws IOException {
        System.out.println(treeSize);
        System.out.println("Tree size: " + treeSize);

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
         //   System.out.println(temp);
            if(bytesReadSoFar == treeSize) break;

        }
        System.out.println(bytesReadSoFar);

        System.out.println("char: " + treeBuilder.charAt(1));

        System.out.println(treeBuilder.length());

        treeBuilder.delete(treeBuilder.length() - treePadding, treeBuilder.length());
        System.out.println(treeBuilder.length());
        System.out.println(treeBuilder);
        CharNode root = traverseTree(n);
        System.out.println(treeBuilder.length());
        System.out.println("Root: " + root.node + " left: " + root.leftNode.node + " right: " + root.rightNode.node);
        printTree(root);
     //   System.out.println(root.isLeaf());
        decodeData(root, bufferedInputStream);
    }
    void printTree(CharNode root) {
        if(root == null) return;
        System.out.println(root.node);
        printTree(root.leftNode);
        printTree(root.rightNode);
    }

    void decodeData(CharNode root, BufferedInputStream bufferedInputStream) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("resultt.pdf");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        CharNode currentNode = root;
        int b;
        System.out.println("hi: " + bufferedInputStream.available());
     //   System.out.println("A is 00011011: " + root.leftNode.leftNode.leftNode.rightNode.rightNode.leftNode.rightNode.rightNode.node);
        while((b = bufferedInputStream.read()) != -1) {

            StringBuilder temp = new StringBuilder();
            String s = Integer.toBinaryString((char) b);
            int len = s.length();
        //    System.out.println("s: " + s);
            while (len < 8) {
                temp.append('0');
                len++;
            }
            temp.append(s);
            if(bufferedInputStream.available() == 1) {
                int pad = bufferedInputStream.read();
                System.out.println("pad: " + pad);
                temp.delete(temp.length() - pad, temp.length());
                System.out.println("After padding: " + temp);
            }

            for(int i = 0; i<temp.length(); i++) {
                if(temp.charAt(i) == '0') {
                    currentNode = currentNode.leftNode;
                }
                else {
                    currentNode = currentNode.rightNode;
                }
                if(currentNode.isLeaf()) {
                    String decodedData = currentNode.node;
                    for(int i1=0; i1<decodedData.length()/8; i1++) {
                        bufferedOutputStream.write((byte)Integer.parseInt(decodedData.substring(8*i1,(i1+1)*8),2));
                    }
                    currentNode = root;
                }
            }

        }
        bufferedOutputStream.close();

    }

    public static void main(String[] args) throws IOException {
        HuffmanDecompression huffmanDecompression = new HuffmanDecompression();
        huffmanDecompression.readCompressedFile("output.txt.hc");
    }
}
