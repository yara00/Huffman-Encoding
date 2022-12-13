import java.io.*;
import java.nio.ByteBuffer;

public class HuffmanDecompression {
    public static int MAX_HEAP_SIZE = 1000 * 100;
    int bitNo = 0;
    StringBuilder treeBuilder = new StringBuilder();
    int extraBytes = 0;
    byte[] extraBytesArray;
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

        extraBytes = bufferedInputStream.read();
        extraBytesArray = new byte[4];
        bufferedInputStream.read(extraBytesArray);
        byte[] treeLen = new byte[4];
        bufferedInputStream.read(treeLen);

        int len = ByteBuffer.wrap(treeLen).getInt();
        int padding = bufferedInputStream.read();
        buildTree(bufferedInputStream, len, padding, bytes);
    }

    CharNode traverseTree(int n) {
        if(treeBuilder.charAt(bitNo) == '1') {// leaf node
            CharNode node = new CharNode(treeBuilder.substring(bitNo + 1, bitNo + 1 + (8*n)));
            bitNo += (1+(8*n));
            return node;
        }
        else {
            bitNo++;
            CharNode newNode = new CharNode(null);
            newNode.leftNode = traverseTree(n);
            newNode.rightNode = traverseTree(n);
            return newNode;
        }

    }
    void buildTree(BufferedInputStream bufferedInputStream, int treeSize, int treePadding, int n) throws IOException {
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
            if(bytesReadSoFar == treeSize) break;

        }

        treeBuilder.delete(treeBuilder.length() - treePadding, treeBuilder.length());
        CharNode root = traverseTree(n);
        decodeData(root, bufferedInputStream, n);
    }


    void decodeData(CharNode root, BufferedInputStream bufferedInputStream, int n) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream("resultnnq.pdf");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ByteBuffer bb = ByteBuffer.allocate(n * n * MAX_HEAP_SIZE);

        CharNode currentNode = root;
        int b;
        while((b = bufferedInputStream.read()) != -1) {
            StringBuilder temp = new StringBuilder();
            String s = Integer.toBinaryString((char) b);
            int len = s.length();
            while (len < 8) {
                temp.append('0');
                len++;
            }
            temp.append(s);
            if(bufferedInputStream.available() == 1) {
                int pad = bufferedInputStream.read();
                temp.delete(temp.length() - pad, temp.length());
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
                    for(int j=0; j<decodedData.length()/8; j++) {
                        bb.put((byte)Integer.parseInt(decodedData.substring(8*j,(j+1)*8),2));
                       // bufferedOutputStream.write((byte)Integer.parseInt(decodedData.substring(8*j,(j+1)*8),2));
                    }
                    currentNode = root;
                }

                if(!bb.hasRemaining()) {
                    bufferedOutputStream.write(bb.array());
                    bb.clear();
                }

            }
        }

        for(int i=0; i<extraBytes; i++) {
            bufferedOutputStream.write((byte)Integer.parseInt(Integer.toBinaryString((byte)extraBytesArray[i] & 0XFF), 2));
        }


        bufferedOutputStream.close();
    }

    public static void main(String[] args) throws IOException {
        long start = System.currentTimeMillis();
        HuffmanDecompression huffmanDecompression = new HuffmanDecompression();
        huffmanDecompression.readCompressedFile("output.txt.hc");
        System.out.println(System.currentTimeMillis() - start);
    }
}
