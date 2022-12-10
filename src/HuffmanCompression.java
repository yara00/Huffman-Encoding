import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class HuffmanCompression {
    String dataContent;
    int bitNo = 0;
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

    StringBuilder readFile(int n, String file, HashMap<String, Integer> frequencyMap) throws IOException {
        List<String> stringList = new ArrayList<>();
        FileInputStream fileInputStream = new FileInputStream(file);
        BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
        int b;
        StringBuilder dataRead = new StringBuilder();
        byte[] line = new byte[n];
        StringBuilder nByteGroup = new StringBuilder();
        while((bufferedInputStream.read(line)) != -1) {
            //nByteGroup.append((char) line[0]);
            //if(n != 1) {
                for(int i=0; i<n; i++) {
                    //b = bufferedInputStream.read();
                    nByteGroup.append((char) line[i]);
                    //if(b == -1) break;
                    line[i] = 0;
                    //nByteGroup.append((char) b);
                }
                dataRead.append(nByteGroup);
                frequencyMap.put(nByteGroup.toString(), frequencyMap.getOrDefault(nByteGroup.toString(), 0) + 1);
           //     stringList.add(nByteGroup.toString());
                nByteGroup.delete(0, nByteGroup.length());
            }
            //else
      //   stringList.add(nByteGroup.toString());
     //   System.out.println("String list: " + stringList.toString());
        dataContent = dataRead.toString();
        bufferedInputStream.close();
        return dataRead;
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

    void buildTable(HashMap<String, String> table, CharNode root, StringBuilder encodedString) {
        if(root == null) return;
        if(root.isLeaf()) {
            table.put(root.node, encodedString.toString());
            return;
        }

        buildTable(table, root.leftNode, encodedString.append('0'));
        encodedString.deleteCharAt(encodedString.length() - 1);
        buildTable(table, root.rightNode, encodedString.append('1'));
        encodedString.deleteCharAt(encodedString.length() - 1);
    }
    void traverseTree(CharNode root, StringBuilder treeBuilder, int n) {
        if(root.isLeaf()) {
            treeBuilder.append(1);
            String rootNode = root.node;
            for(int i=0; i<rootNode.length(); i++) {
                String charNode = Integer.toBinaryString(rootNode.charAt(i));
                if(charNode.length() < 8) {
                    for(int j=charNode.length(); j<8; j++) {
                        treeBuilder.append(0);
                    }
                }
                treeBuilder.append(charNode);
            }
        }
        else {
            treeBuilder.append(0);
            traverseTree(root.leftNode, treeBuilder, n);
            traverseTree(root.rightNode, treeBuilder, n);
        }
    }
    BitSet buildTableHeader(int n, CharNode rootNode) throws IOException {
        BitSet treeBit = new BitSet();
        StringBuilder stringBuilder = new StringBuilder();
        String nBytes = Integer.toBinaryString(n);
        for(int i=nBytes.length()+1; i<=8; i++) stringBuilder.append(0);
        stringBuilder.append(nBytes);
        System.out.println("N is: " + stringBuilder.toString().getBytes(StandardCharsets.UTF_8));
        traverseTree(rootNode, stringBuilder, n);
        for(Character bitChar: stringBuilder.toString().toCharArray()) {
            if(bitChar.equals('1')) treeBit.set(bitNo);
            else treeBit.clear(bitNo);
            bitNo++;
        }
        
   //     System.out.println("BuilderLen: " + stringBuilder.);
        stringBuilder.delete(0, stringBuilder.length());
        return treeBit;
    }
    BitSet buildEncodedContent(int n, BitSet treeBit, HashMap<String, String> encodingMap) {
        StringBuilder encodedContent = new StringBuilder();
        for(Character character : dataContent.toCharArray()) {
            StringBuilder nByteGroup = new StringBuilder();
            int count = 0;
            while (count < n) {
                nByteGroup.append(character);
                count++;
            }
            encodedContent.append(encodingMap.get(nByteGroup.toString()));
            nByteGroup.delete(0, nByteGroup.length());
        }
    //    System.out.println("encoded: " + encodedContent);
     //   System.out.println("bitno before: " + bitNo);
        for (Character c : encodedContent.toString().toCharArray()) {
            if(c.equals('1')) treeBit.set(bitNo);
            else treeBit.clear(bitNo);
            bitNo++;
        }
      //  System.out.println("bitno after: " + bitNo);

        return treeBit;
    }
    void compressionAlgorithm(String file, int n) throws IOException {
        // build a hashmap of each unique character as a key associated with its frequency as a value
        HashMap<String, Integer> frequencyMap = new HashMap<>();

        StringBuilder stringList = readFile(n, file, frequencyMap);//List.of("g", "o", " ", "g", "o", " ", "g", "o", "p", "h", "e", "r", "s");//readFile(n, file);
/*
        for (String s : stringList.) {
            frequencyMap.put(s, frequencyMap.getOrDefault(s, 0) + 1);
        }

 */

        PriorityQueue<CharNode> freqPriority = new PriorityQueue<>((a,b) -> a.frequency - b.frequency);
        for (String character : frequencyMap.keySet()) {
          //  System.out.println("Key " + character + " value= " + frequencyMap.get(character));
            freqPriority.add(new CharNode(character, frequencyMap.get(character)));
        }
/*
        for (CharNode character : freqPriority) {
            System.out.println(character.node + " " + character.frequency);
        }
*/
        CharNode rootNode = buildTree(freqPriority);
        HashMap<String, String> encodingMap = new HashMap<>();
        StringBuilder encodedString = new StringBuilder();
        buildTable(encodingMap, rootNode, encodedString);
/*
        for (String character : encodingMap.keySet()) {
            System.out.println("Key: " + character + " value= " + encodingMap.get(character));
        }

 */
        BitSet treeBit = buildTableHeader(n, rootNode);
        treeBit = buildEncodedContent(n, treeBit, encodingMap);
        FileOutputStream fileOutputStream = new FileOutputStream("output.txt.hc");
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
      //  System.out.println("Treebit size= " + treeBit.size() + " builder= " + encodedString.length());
        bufferedOutputStream.write(treeBit.toByteArray());
       // System.out.println(treeBit.toByteArray().length);
        bufferedOutputStream.close();
    }

    public static void main(String[] args) throws IOException {
        HuffmanCompression huffman = new HuffmanCompression();
        long start = System.currentTimeMillis();
        huffman.compressionAlgorithm("C:/Users/Dell/Downloads/Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf",
                2);//Algorithms - Lectures 7 and 8 (Greedy algorithms).pdf", 1); gbbct10.seq
        System.out.println(System.currentTimeMillis() - start);
    }
}
