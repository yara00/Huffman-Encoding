import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HuffmanCompression {
    public static int MAX_HEAP_SIZE = 1024;
    String dataContent;
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

    void readFile(int n, String file, HashMap<String, Integer> frequencyMap, StringBuilder dataRead) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        //StringBuilder dataRead = new StringBuilder();
     //   byte[] bytesToRead = new byte[n];
        StringBuilder nByteGroup = new StringBuilder();
        int read;
        while((read = bufferedInputStream.read()) != -1) {
                for(int i=0; i<n; i++) {
                    nByteGroup.append((char) (byte) read);
              //      bytesToRead[i] = 0;
                }
                dataRead.append(nByteGroup);
                frequencyMap.put(nByteGroup.toString(), frequencyMap.getOrDefault(nByteGroup.toString(), 0) + 1);
                nByteGroup.delete(0, nByteGroup.length());
            }
        bufferedInputStream.close();
        dataContent = dataRead.toString();
        dataRead.delete(0, dataRead.length());
        return;
    }

    CharNode buildTree(PriorityQueue<CharNode> priorityQueue) {
        while (priorityQueue.size() != 1) {
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
          //  System.out.println("Root node: " + root.node);
            table.put(root.node, encodedString.toString());
            return;
        }
        System.out.println("encoded abl: " + encodedString);
        buildPrefixCodeTable(table, root.leftNode, encodedString.append(0));
        System.out.println("encoded b3d: " + encodedString);

        encodedString.deleteCharAt(encodedString.length() - 1);
        buildPrefixCodeTable(table, root.rightNode, encodedString.append(1));
        encodedString.deleteCharAt(encodedString.length() - 1);
    }
    void traverseTree(CharNode root, StringBuilder treeBuilder, BufferedOutputStream bufferedOutputStream) throws IOException {
        if(root.isLeaf()) {
            treeBuilder.append(1);
            String rootNode = root.node;
            for(int i=0; i<rootNode.length(); i++) {
                String charNode = Integer.toBinaryString(rootNode.charAt(i));
                // append leading zeros
                if(charNode.length() < 8) {
                    for(int j=charNode.length(); j<8; j++) {
                        treeBuilder.append(0);
                    }
                }
              //  System.out.println("sizeeee: " + rootNode.charAt(i));
                treeBuilder.append(charNode);
                if((treeBuilder.length() / 8) >= MAX_HEAP_SIZE) {
                    //   System.out.println("at char: " + charNode);
                    byte[] bytesToWrite = new byte[MAX_HEAP_SIZE];
                    for(int bit=0; bit<(8*MAX_HEAP_SIZE); bit+=8) {
                      //  (byte)Integer.parseInt(stringBuilder.toString(), 2);
                        bytesToWrite[bit/8] = (byte)Integer.parseInt(treeBuilder.substring(bit, bit+8), 2);

                    }
            //        System.out.println("Written: " + bytesToWrite.length);
                    bufferedOutputStream.write(bytesToWrite);
                  //  System.out.println("Byte len: " + treeBuilder.substring(0, MAX_HEAP_SIZE * 8).getBytes().length);
                    bufferedOutputStream.flush();
                    treeBuilder.delete(0, MAX_HEAP_SIZE * 8);
                }
            }
        }
        else {
            treeBuilder.append(0);
            traverseTree(root.leftNode, treeBuilder, bufferedOutputStream);
            traverseTree(root.rightNode, treeBuilder, bufferedOutputStream);
        }
    }
    void buildTableHeaderAndTree(int n, CharNode rootNode, StringBuilder stringBuilder, BufferedOutputStream bufferedOutputStream) throws IOException {
        String nBytes = Integer.toBinaryString(n);
        for(int i=nBytes.length()+1; i<=8; i++) stringBuilder.append(0);
        stringBuilder.append(nBytes);
        byte bytesToWrite = (byte)Integer.parseInt(stringBuilder.toString(), 2);
        bufferedOutputStream.write(bytesToWrite);
        bufferedOutputStream.flush();
        stringBuilder.delete(0, stringBuilder.length());
        traverseTree(rootNode, stringBuilder, bufferedOutputStream);
       /* if (stringBuilder.length() != 0) {
            bufferedOutputStream.write(stringBuilder.toString().getBytes());
            bufferedOutputStream.flush();
            stringBuilder.delete(0, stringBuilder.length());
        }

        */
    }

    void buildEncodedContent(int n, HashMap<String, String> encodingMap, BufferedOutputStream bufferedOutputStream, StringBuilder encodedContent) throws IOException {
      //  StringBuilder encodedContent = new StringBuilder();
        for(Character character : dataContent.toCharArray()) {
            StringBuilder nByteGroup = new StringBuilder();
            int count = 0;
            while (count < n) {
                nByteGroup.append(character);
                count++;
            }
            //   System.out.println("Encoded: "+ encodingMap.get(nByteGroup.toString()) + "key= " + nByteGroup.toString());
            encodedContent.append(encodingMap.get(nByteGroup.toString()));
            nByteGroup.delete(0, nByteGroup.length());

            // write to file in chuncks
            if((encodedContent.length() / 8) >= MAX_HEAP_SIZE) {
                //   System.out.println("at char: " + charNode);
                byte[] bytesToWrite = new byte[MAX_HEAP_SIZE];
                for(int bit=0; bit<(8*MAX_HEAP_SIZE); bit+=8) {
                    //  (byte)Integer.parseInt(stringBuilder.toString(), 2);
                    bytesToWrite[bit/8] = (byte)Integer.parseInt(encodedContent.substring(bit, bit+8), 2);

                }
                bufferedOutputStream.write(bytesToWrite);
                //  System.out.println("Byte len: " + treeBuilder.substring(0, MAX_HEAP_SIZE * 8).getBytes().length);
                bufferedOutputStream.flush();
                encodedContent.delete(0, MAX_HEAP_SIZE * 8);
            }
            /*
            if(((encodedContent.length() / 8) >= MAX_HEAP_SIZE) && (encodedContent.length() % 8 == 0)) {
                bufferedOutputStream.write(encodedContent.toString().getBytes());
                bufferedOutputStream.flush();
                encodedContent.delete(0, encodedContent.length());
            }
            */
        }

        // write lagging bits
        if (encodedContent.length() != 0) {
            if(encodedContent.length() % 8 != 0)  {
                int remainder = 8 - (encodedContent.length() % 8);
                for(int j=0; j<remainder; j++) encodedContent.append(0);
            }
            byte[] bytesToWrite = new byte[encodedContent.length()];
            for(int bit=0; bit<(encodedContent.length()); bit+=8) {
                //  (byte)Integer.parseInt(stringBuilder.toString(), 2);
                bytesToWrite[bit/8] = (byte)Integer.parseInt(encodedContent.substring(bit, bit+8), 2);
            }
         //   System.out.println("Written: " + bytesToWrite.length);
            bufferedOutputStream.write(bytesToWrite);
            //  System.out.println("Byte len: " + treeBuilder.substring(0, MAX_HEAP_SIZE * 8).getBytes().length);
            bufferedOutputStream.flush();
            encodedContent.delete(0, encodedContent.length());
            /*
            bufferedOutputStream.write(encodedContent.toString().getBytes());
            bufferedOutputStream.flush();
            encodedContent.delete(0, encodedContent.length());

             */
        }
    }

    void compressionAlgorithm(String file, int n) throws IOException {
        // build a hashmap of each unique character as a key associated with its frequency as a value
        HashMap<String, Integer> frequencyMap = new HashMap<>();
        StringBuilder stringBuilder = new StringBuilder();
        readFile(n, file, frequencyMap, stringBuilder);//List.of("g", "o", " ", "g", "o", " ", "g", "o", "p", "h", "e", "r", "s");//readFile(n, file);


        PriorityQueue<CharNode> freqPriority = new PriorityQueue<>((a,b) -> a.frequency - b.frequency);
        for (String character : frequencyMap.keySet()) {
            freqPriority.add(new CharNode(character, frequencyMap.get(character)));
        }

        CharNode rootNode = buildTree(freqPriority);
        HashMap<String, String> encodingMap = new HashMap<>();
        StringBuilder encodedString = new StringBuilder();
      //  System.out.println("root da5la " + rootNode.node);
        buildPrefixCodeTable(encodingMap, rootNode, encodedString);

        for (String s : encodingMap.keySet()) {
        //    System.out.println("Key: " + s + " value= " + encodingMap.get(s));

        }


        FileOutputStream fileOutputStream = new FileOutputStream("output.txt.hc");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
        buildTableHeaderAndTree(n, rootNode, stringBuilder, bufferedOutputStream);
        buildEncodedContent(n, encodingMap, bufferedOutputStream, stringBuilder);
        bufferedOutputStream.close();
    }

    public static void main(String[] args) throws IOException {
        HuffmanCompression huffman = new HuffmanCompression();
        long start = System.currentTimeMillis();
        System.out.println("yara");
        System.out.println("caa".length());
        StringBuilder s= new StringBuilder();
        String charNode = Integer.toBinaryString('c');
        s.append(charNode);
        System.out.println("s: " + s.length());
        s.append('0');
        System.out.println("s: " + s.length());

         huffman.compressionAlgorithm("C:/Users/Dell/Downloads/Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf",
                1);//Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf", 1); gbbct10.seq Desktop/aa.txt

        System.out.println(System.currentTimeMillis() - start);
    }
}
/*
tree len tree padding data padding extra bytes
 */
