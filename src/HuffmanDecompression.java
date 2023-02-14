import java.io.*;
import java.nio.ByteBuffer;

public class HuffmanDecompression {
    public static int MAX_HEAP_SIZE = 1000 * 100;
    int bitNo = 0;
    StringBuilder treeBuilder = new StringBuilder();
    int dataSize;
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
    void readCompressedFile(String file, String resultPath) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        // n value
        byte[] nBytes = new byte[4];
        bufferedInputStream.read(nBytes);
        int bytes = ByteBuffer.wrap(nBytes).getInt();

        // read extra bytes no and values
        extraBytes = bufferedInputStream.read();
        extraBytesArray = new byte[4];
        bufferedInputStream.read(extraBytesArray);

        // read tree length in bytes
        byte[] treeLen = new byte[4];
        bufferedInputStream.read(treeLen);
        int len = ByteBuffer.wrap(treeLen).getInt();

        // read encoded data length in bytes
        byte[] dataLen = new byte[4];
        bufferedInputStream.read(dataLen);
        dataSize = ByteBuffer.wrap(dataLen).getInt();
        dataSize = (int) Math.ceil(dataSize/8.0);  // ceil to include last byte with padded bits
        int treePadding = bufferedInputStream.read();

        buildTree(bufferedInputStream, len, treePadding, bytes, resultPath);
    }

    CharNode traverseTree(int n) {
        if(treeBuilder.charAt(bitNo) == '1') {  // leaf node
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
    void buildTree(BufferedInputStream bufferedInputStream, int treeSize, int treePadding, int n, String resultPath) throws IOException {
        int bytesReadSoFar = 0; // limit bytes read to tree bytes only
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
        // remove padding bits
        treeBuilder.delete(treeBuilder.length() - treePadding, treeBuilder.length());
        CharNode root = traverseTree(n);    // acquire root
        decodeData(root, bufferedInputStream, n, resultPath);
    }


    void decodeData(CharNode root, BufferedInputStream bufferedInputStream, int n, String resultPath) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(resultPath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        ByteBuffer bb = ByteBuffer.allocate(n * n * MAX_HEAP_SIZE);

        CharNode currentNode = root;
        int b;
        int bufferIndex = 0;
        while((b = bufferedInputStream.read()) != -1) {
            dataSize--;
            StringBuilder temp = new StringBuilder();
            String s = Integer.toBinaryString((char) b);
            int len = s.length();
            while (len < 8) {
                temp.append('0');
                len++;
            }
            temp.append(s);

            if(dataSize == 0) {
                int pad = bufferedInputStream.read();
                temp.delete(temp.length() - pad, temp.length());    // delete padding bits
            }

            for(int i = 0; i<temp.length(); i++) {
                if(temp.charAt(i) == '0') { // move left
                    currentNode = currentNode.leftNode;
                }
                else {  // move right
                    currentNode = currentNode.rightNode;
                }
                if(currentNode.isLeaf()) {
                    String decodedData = currentNode.node;
                    for(int j=0; j<decodedData.length()/8; j++) {
                        bb.put((byte)Integer.parseInt(decodedData.substring(8*j,(j+1)*8),2));
                        bufferIndex++;
                    }
                    currentNode = root;
                }
                if(!bb.hasRemaining()) {    // max buffer
                    bufferedOutputStream.write(bb.array());
                    bufferedOutputStream.flush();
                    bb.clear();
                    bufferIndex = 0;
                }
            }
        }
        // write trailing bytes
        for(int k=0; k<bufferIndex; k++) {
            bufferedOutputStream.write(bb.get(k));
        }
        // write extra bytes
        for(int i=0; i<extraBytes; i++) {
            bufferedOutputStream.write((byte)Integer.parseInt(Integer.toBinaryString((byte)extraBytesArray[i] & 0XFF), 2));
        }
        bufferedOutputStream.close();
    }


}
