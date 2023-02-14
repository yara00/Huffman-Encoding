import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class HuffmanCompression {
    public static int MAX_HEAP_SIZE = 1000 * 100;
    int dataSize;
    int extraBytes;
    long fileSizeInBytes;
    long bytesToRead;
    byte[] extraBytesArray = {0, 0, 0, 0};
    HashMap<String, Integer> frequencyMap = new HashMap<>();
    class CharNode {
        String node;
        Integer frequency;
        CharNode leftNode = null;
        CharNode rightNode = null;

        public CharNode(String node, Integer frequency) {
            this.node = node;
            this.frequency = frequency;
        }
        public boolean isLeaf() {
            return (this.leftNode == null && this.rightNode == null);
        }
    }

    void readFile(int n, String file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        byte[] nBytes = new byte[n];
        StringBuilder nByteGroup = new StringBuilder();
        while((bufferedInputStream.read(nBytes)) != -1) {
            bytesToRead -= n;
            for(int i=0; i<n; i++) {
                nByteGroup.append((char) nBytes[i]);
                nBytes[i] = 0;
            }
            frequencyMap.put(nByteGroup.toString(), frequencyMap.getOrDefault(nByteGroup.toString(), 0) + 1);
            nByteGroup.delete(0, nByteGroup.length());
            if(bytesToRead == 0) break;
        }
        // write extra bytes separately
        bufferedInputStream.read(extraBytesArray);
        bufferedInputStream.close();
    }

    CharNode buildTree(PriorityQueue<CharNode> priorityQueue) {
        while (priorityQueue.size() > 1) {
            CharNode leftNode = priorityQueue.poll();
            CharNode rightNode = priorityQueue.poll();
            Integer frequencySum = leftNode.frequency + rightNode.frequency;
            CharNode newNode = new CharNode(null, frequencySum);
            newNode.leftNode = leftNode;
            newNode.rightNode = rightNode;
            newNode.frequency = frequencySum;
            priorityQueue.add(newNode);
        }
        return priorityQueue.peek();    // root node
    }

    void buildPrefixCodeTable(HashMap<String, String> table, CharNode root, StringBuilder encodedString) {
        if(root == null) return;
        if(root.isLeaf()) {
            table.put(root.node, encodedString.toString());
            return;
        }
        // left node
        encodedString.append('0');
        buildPrefixCodeTable(table, root.leftNode, encodedString);
        encodedString.deleteCharAt(encodedString.length() - 1);
        // right node
        encodedString.append('1');
        buildPrefixCodeTable(table, root.rightNode, encodedString);
        encodedString.deleteCharAt(encodedString.length() - 1);
    }

    void traverseTree(CharNode root, StringBuilder treeBuilder) {
        if(root.isLeaf()) {
            treeBuilder.append('1');    // leaf node
            String rootNode = root.node;
            for(int i=0; i<rootNode.length(); i++) {
                String charNode = Integer.toBinaryString((byte)rootNode.charAt(i) & 0XFF);
                // append leading zeros
                if(charNode.length() < 8) {
                    for(int j=charNode.length(); j<8; j++) {
                        treeBuilder.append('0');
                    }
                }
                treeBuilder.append(charNode);
            }
        }
        else {
            treeBuilder.append('0');
            traverseTree(root.leftNode, treeBuilder);
            traverseTree(root.rightNode, treeBuilder);
        }
    }

    void buildTableHeaderAndTree(int n, CharNode rootNode, StringBuilder stringBuilder, BufferedOutputStream bufferedOutputStream) throws IOException {
        // write n integer
        ByteBuffer bbBytes = ByteBuffer.allocate(4);
        bbBytes.putInt(n);
        bufferedOutputStream.write(bbBytes.array());
        // write extra bytes number and values
        bufferedOutputStream.write((byte) extraBytes);
        bufferedOutputStream.write(extraBytesArray);
        bufferedOutputStream.flush();
        stringBuilder.delete(0, stringBuilder.length());

        traverseTree(rootNode, stringBuilder);

        int len;
        if (stringBuilder.length() % 8 == 0) len = stringBuilder.length() / 8;
        else len = (stringBuilder.length() / 8) + 1;
        // write tree length in bytes
        ByteBuffer bbTree = ByteBuffer.allocate(4);
        bbTree.putInt(len);
        bufferedOutputStream.write(bbTree.array());

        // write encoded data length in bytes
        ByteBuffer bbData = ByteBuffer.allocate(4);
        bbData.putInt(dataSize);
        bufferedOutputStream.write(bbData.array());

        // write tree in header
        byte[] headerBytesToWrite = new byte[len];
        for (int bit = 0; bit < (8 * (len-1)); bit += 8) {
            headerBytesToWrite[bit / 8] = (byte) Integer.parseInt(stringBuilder.substring(bit, bit + 8), 2);
        }
        stringBuilder.delete(0, 8 * (len-1));
        // tree padding
        bufferedOutputStream.write((byte) (8 - stringBuilder.length()));
        for(int i=stringBuilder.length(); i<8; i++) stringBuilder.append('0');
        headerBytesToWrite[len - 1] = (byte) Integer.parseInt(stringBuilder.toString(), 2);
        bufferedOutputStream.write(headerBytesToWrite);
        bufferedOutputStream.flush();
        stringBuilder.delete(0, stringBuilder.length());
    }

    void buildEncodedData(int n, HashMap<String, String> encodingMap, BufferedOutputStream bufferedOutputStream, StringBuilder encodedContent, String file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        byte[] nBytes = new byte[n];
        StringBuilder nByteGroup = new StringBuilder();
        while((bufferedInputStream.read(nBytes)) != -1) {
            bytesToRead -= n;
            for(int i=0; i<n; i++) {
                nByteGroup.append((char) nBytes[i]);
                nBytes[i] = 0;
            }
            encodedContent.append(encodingMap.get(nByteGroup.toString()));
            nByteGroup.delete(0, nByteGroup.length());

            // write to file in chunks
            if((encodedContent.length() / 8) >= MAX_HEAP_SIZE) {
                byte[] bytesToWrite = new byte[MAX_HEAP_SIZE];
                for(int bit=0; bit<(8*MAX_HEAP_SIZE); bit+=8) {
                    bytesToWrite[bit/8] = (byte)Integer.parseInt(encodedContent.substring(bit, bit+8), 2);
                }
                bufferedOutputStream.write(bytesToWrite);
                bufferedOutputStream.flush();
                encodedContent.delete(0, MAX_HEAP_SIZE * 8);
            }
          //  if(bufferedInputStream.available() == extraBytes) break;
            if(bytesToRead == 0) break;
        }

        // write trailing bits
        int remainder = 0;
        if (encodedContent.length() != 0) {
            if(encodedContent.length() % 8 != 0)  {
                remainder = 8 - (encodedContent.length() % 8);
                for(int j=0; j<remainder; j++) encodedContent.append('0');
            }
            byte[] bytesToWrite = new byte[encodedContent.length() / 8];
            for(int bit=0; bit<(encodedContent.length()); bit+=8) {
                bytesToWrite[bit/8] = (byte)Integer.parseInt(encodedContent.substring(bit, bit+8), 2);
            }
            bufferedOutputStream.write(bytesToWrite);
            bufferedOutputStream.write((byte) remainder);
            bufferedOutputStream.flush();
            encodedContent.delete(0, encodedContent.length());
        }
    }

    void countDataBytes(HashMap<String, String> encodingMap) {
        for (String s : encodingMap.keySet()) {
            dataSize += (frequencyMap.get(s) * encodingMap.get(s).length());
        }
    }
    void compressionAlgorithm(String file, int n, String resultPath) throws IOException {
        File fileInput = new File(file);
        fileSizeInBytes = fileInput.length();
        extraBytes = (int) (fileInput.length() % n);
        bytesToRead = fileSizeInBytes - extraBytes;
        // build a hashmap of each unique character as a key associated with its frequency as a value
        StringBuilder stringBuilder = new StringBuilder();
        readFile(n, file);

        bytesToRead = fileSizeInBytes - extraBytes;
        PriorityQueue<CharNode> freqPriority = new PriorityQueue<>((a,b) -> a.frequency - b.frequency);
        for (String character : frequencyMap.keySet()) {
            freqPriority.add(new CharNode(character, frequencyMap.get(character)));
        }

        CharNode rootNode = buildTree(freqPriority);

        HashMap<String, String> encodingMap = new HashMap<>();
        StringBuilder encodedString = new StringBuilder();
        buildPrefixCodeTable(encodingMap, rootNode, encodedString);
        encodedString.delete(0, encodedString.length());

        FileOutputStream fileOutputStream = new FileOutputStream(resultPath);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        countDataBytes(encodingMap);

        buildTableHeaderAndTree(n, rootNode, stringBuilder, bufferedOutputStream);

        buildEncodedData(n, encodingMap, bufferedOutputStream, stringBuilder, file);

        bufferedOutputStream.close();
    }
}