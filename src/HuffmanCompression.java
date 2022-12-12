import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

public class HuffmanCompression {
    public static int MAX_HEAP_SIZE = 1024;
    int extraBytes;
    long fileSizeInBytes;
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

    void readFile(int n, String file, StringBuilder dataRead) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);

        byte[] bytesToRead = new byte[n];
        StringBuilder nByteGroup = new StringBuilder();
        while((bufferedInputStream.read(bytesToRead)) != -1) {
            for(int i=0; i<n; i++) {
                    nByteGroup.append((char) bytesToRead[i]);
                    bytesToRead[i] = 0;
            }
            dataRead.append(nByteGroup);
            frequencyMap.put(nByteGroup.toString(), frequencyMap.getOrDefault(nByteGroup.toString(), 0) + 1);
            nByteGroup.delete(0, nByteGroup.length());
        }

        bufferedInputStream.close();
        dataRead.delete(0, dataRead.length());
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
        return priorityQueue.peek();
    }

    void buildPrefixCodeTable(HashMap<String, String> table, CharNode root, StringBuilder encodedString) {
        if(root == null) return;
        if(root.isLeaf()) {
            table.put(root.node, encodedString.toString());
            return;
        }
        encodedString.append('0');
        buildPrefixCodeTable(table, root.leftNode, encodedString);

        encodedString.deleteCharAt(encodedString.length() - 1);
        encodedString.append('1');
        buildPrefixCodeTable(table, root.rightNode, encodedString);
        encodedString.deleteCharAt(encodedString.length() - 1);
    }
    void traverseTree(CharNode root, StringBuilder treeBuilder, BufferedOutputStream bufferedOutputStream) throws IOException {
        if(root.isLeaf()) {
            treeBuilder.append('1');
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
            traverseTree(root.leftNode, treeBuilder, bufferedOutputStream);
            traverseTree(root.rightNode, treeBuilder, bufferedOutputStream);
        }
    }
    void buildTableHeaderAndTree(int n, CharNode rootNode, StringBuilder stringBuilder, BufferedOutputStream bufferedOutputStream) throws IOException {
        String nBytes = Integer.toBinaryString(n);
        for (int i = nBytes.length() + 1; i <= 8; i++) stringBuilder.append('0');
        stringBuilder.append(nBytes);
        byte bytesToWrite = (byte) Integer.parseInt(stringBuilder.toString(), 2);
        bufferedOutputStream.write(bytesToWrite);
        bufferedOutputStream.write((byte) extraBytes);
      //  bufferedOutputStream.write(extraBytesArray);
        bufferedOutputStream.flush();
        stringBuilder.delete(0, stringBuilder.length());

        traverseTree(rootNode, stringBuilder, bufferedOutputStream);
      //  System.out.println(stringBuilder.substring(0, stringBuilder.length()));

        int len;
        if (stringBuilder.length() % 8 == 0) len = stringBuilder.length() / 8;
        else len = (stringBuilder.length() / 8) + 1;
     //   System.out.println("LEN: " + len + " aaaa " + stringBuilder.length());
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(len);
        bufferedOutputStream.write(bb.array());

        byte[] headerBytesToWrite = new byte[len];
        for (int bit = 0; bit < (8 * (len-1)); bit += 8) {
            headerBytesToWrite[bit / 8] = (byte) Integer.parseInt(stringBuilder.substring(bit, bit + 8), 2);
        }
        System.out.println("mn hena");
        System.out.println(stringBuilder.substring(0, 8*(len-1)));
        stringBuilder.delete(0, 8 * (len-1));
        // tree padding
        System.out.println("Tree padding: " + (8 - stringBuilder.length()));
        bufferedOutputStream.write((byte) (8 - stringBuilder.length()));
        for(int i=stringBuilder.length(); i<8; i++) stringBuilder.append('0');
     //   System.out.println(stringBuilder);
        headerBytesToWrite[len - 1] = (byte) Integer.parseInt(stringBuilder.toString(), 2);
        bufferedOutputStream.write(headerBytesToWrite);
        bufferedOutputStream.flush();
        stringBuilder.delete(0, stringBuilder.length());
    }

    void buildEncodedContent(int n, HashMap<String, String> encodingMap, BufferedOutputStream bufferedOutputStream, StringBuilder encodedContent, String file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        byte[] bytesToRead = new byte[n];
        StringBuilder nByteGroup = new StringBuilder();
        while((bufferedInputStream.read(bytesToRead)) != -1) {
            for(int i=0; i<n; i++) {
                nByteGroup.append((char) bytesToRead[i]);
                bytesToRead[i] = 0;
            }
            encodedContent.append(encodingMap.get(nByteGroup.toString()));
            nByteGroup.delete(0, nByteGroup.length());
        //    System.out.println("encoded: " + encodedContent.toString());
            // write to file in chuncks
            if((encodedContent.length() / 8) >= MAX_HEAP_SIZE) {
                byte[] bytesToWrite = new byte[MAX_HEAP_SIZE];
                for(int bit=0; bit<(8*MAX_HEAP_SIZE); bit+=8) {
                    bytesToWrite[bit/8] = (byte)Integer.parseInt(encodedContent.substring(bit, bit+8), 2);
                }
                bufferedOutputStream.write(bytesToWrite);
                bufferedOutputStream.flush();
                encodedContent.delete(0, MAX_HEAP_SIZE * 8);
            }
        }

        // write trailing bits
        int remainder = 0;
        if (encodedContent.length() != 0) {
            if(encodedContent.length() % 8 != 0)  {
                remainder = 8 - (encodedContent.length() % 8);
                for(int j=0; j<remainder; j++) encodedContent.append('0');
            }
            System.out.println("rem: " + remainder);
            byte[] bytesToWrite = new byte[encodedContent.length() / 8];
            System.out.println("ennnn: " + encodedContent);
            for(int bit=0; bit<(encodedContent.length()); bit+=8) {
                bytesToWrite[bit/8] = (byte)Integer.parseInt(encodedContent.substring(bit, bit+8), 2);
            }
            System.out.println("to write: " + bytesToWrite.length);
            bufferedOutputStream.write(bytesToWrite);
            bufferedOutputStream.write((byte) remainder);
            bufferedOutputStream.flush();
     //       bufferedOutputStream.close();
            encodedContent.delete(0, encodedContent.length());
            }
        }
    void compressionAlgorithm(String file, int n) throws IOException {
        File fileInput = new File(file);
        fileSizeInBytes = fileInput.length();
        extraBytes = (int) (fileInput.length() % n);
     //   System.out.println(extraBytes);
        // build a hashmap of each unique character as a key associated with its frequency as a value
       // HashMap<String, Integer> frequencyMap = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        readFile(n, file, stringBuilder);

        PriorityQueue<CharNode> freqPriority = new PriorityQueue<>((a,b) -> a.frequency - b.frequency);
        for (String character : frequencyMap.keySet()) {
            freqPriority.add(new CharNode(character, frequencyMap.get(character)));
        }

        CharNode rootNode = buildTree(freqPriority);
        System.out.println("ROot: " + rootNode.node + " left: " + rootNode.leftNode.node + " right: " + rootNode.rightNode.node);
        HashMap<String, String> encodingMap = new HashMap<>();
        StringBuilder encodedString = new StringBuilder();
        buildPrefixCodeTable(encodingMap, rootNode, encodedString);
     /*   for (String s : encodingMap.keySet()) {
            System.out.println("key= " + s + " value= " + encodingMap.get(s));

        }

      */
        encodedString.delete(0, encodedString.length());
        FileOutputStream fileOutputStream = new FileOutputStream("output.txt.hc");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
       // System.out.println("Building header and tree...");
        buildTableHeaderAndTree(n, rootNode, stringBuilder, bufferedOutputStream);
     //   System.out.println("Building encoded data...");
        buildEncodedContent(n, encodingMap, bufferedOutputStream, stringBuilder, file);
        bufferedOutputStream.close();
    }

    public static void main(String[] args) throws IOException {
        HuffmanCompression huffman = new HuffmanCompression();
        long start = System.currentTimeMillis();

         huffman.compressionAlgorithm("C:/Users/Dell/Downloads/Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf",
                2);//Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf", 1); gbbct10.seq Desktop/aa.txt

        System.out.println(System.currentTimeMillis() - start);
    }
}
/*
tree len tree padding data padding extra bytes
 */

