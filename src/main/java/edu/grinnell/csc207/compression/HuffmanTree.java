package edu.grinnell.csc207.compression;

import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;

/**
 * A HuffmanTree derives a space-efficient coding of a collection of byte
 * values.
 *
 * The huffman tree encodes values in the range 0--255 which would normally
 * take 8 bits. However, we also need to encode a special EOF character to
 * denote the end of a .grin file. Thus, we need 9 bits to store each
 * byte value. This is fine for file writing (modulo the need to write in
 * byte chunks to the file), but Java does not have a 9-bit data type.
 * Instead, we use the next larger primitive integral type, short, to store
 * our byte values.
 */
public class HuffmanTree {

    private class Node implements Comparable<Node> {
        short value;
        int freq;
        Node left;
        Node right;

        public Node(short value, int freq) {
            this.value = value;
            this.freq = freq;
        }

        public Node(Node left, Node right) {
            this.freq = left.freq + right.freq;
            this.left = left;
            this.right = right;
        }

        public int compareTo(Node other) {
            return Integer.compare(this.freq, other.freq);
        }

        public boolean isLeaf() {
            return (this instanceof Leaf);
        }
    }

    private class Leaf extends Node {
        public Leaf(short value, int freq) {
            super(value, freq);
        }
    }

    private class Branch extends Node {
        public Branch(Node left, Node right) {
            super(left, right);
        }
    }

    private Node root;

    /**
     * Constructs a new HuffmanTree from a frequency map.
     * 
     * @param freqs a map from 9-bit values to frequencies.
     */
    public HuffmanTree(Map<Short, Integer> freqs) {
        PriorityQueue<Node> pq = new PriorityQueue<>();

        for (Map.Entry<Short, Integer> e : freqs.entrySet()) {
            pq.add(new Leaf(e.getKey(), e.getValue()));
        }

        short EOF = 256;
        pq.add(new Leaf(EOF, 1));

        while (pq.size() > 1) {
            Node x = pq.remove();
            Node y = pq.remove();

            pq.add(new Branch(x, y));
        }

        this.root = pq.remove();
    }

    /**
     * Constructs a new HuffmanTree from the given file.
     * 
     * @param in the input file (as a BitInputStream)
     */
    public HuffmanTree(BitInputStream in) {
        this.root = readTree(in);
    }

    private Node readTree(BitInputStream in) {
        int bit = in.readBit();
        if (bit == 0) {
            short value = (short) in.readBits(9);
            return new Leaf(value, 0);
        } else { // internal
            Node left = readTree(in);
            Node right = readTree(in);
            return new Branch(left, right);
        }
    }

    /**
     * Writes this HuffmanTree to the given file as a stream of bits in a
     * serialized format.
     * 
     * @param out the output file as a BitOutputStream
     */
    public void serialize(BitOutputStream out) {
        serializeH(root, out);
    }

    public void serializeH(Node n, BitOutputStream out) {
        if (n.isLeaf()) {
            out.writeBit(0);
            out.writeBits(n.value, 9);
        } else {
            out.writeBit(1);
            serializeH(n.left, out);
            serializeH(n.right, out);
        }
    }

    /**
     * Encodes the file given as a stream of bits into a compressed format
     * using this Huffman tree. The encoded values are written, bit-by-bit
     * to the given BitOuputStream.
     * 
     * @param in  the file to compress.
     * @param out the file to write the compressed output to.
     */
    public void encode(BitInputStream in, BitOutputStream out) {
        Map<Short, String> hm = new HashMap<>();
        encodeH(root, "", hm);

        while (true) {
            short key = (short) in.readBits(8);
            if (key == -1) {
                break; // Hit end of file
            }
            String value = hm.get(key);
            for (char c : value.toCharArray()) {
                if (c == '0') {
                    out.writeBit(0);
                } else {
                    out.writeBit(1);
                }
            }
        }
        String eofVal = hm.get((short) 256);
        for (char c : eofVal.toCharArray()) {
            if (c == '0') {
                out.writeBit(0);
            } else {
                out.writeBit(1);
            }
        }
    }

    private void encodeH(Node n, String path, Map<Short, String> map) {
        if (n.isLeaf()) {
            map.put(n.value, path);
            return;
        }
        encodeH(n.left, path + "0", map);
        encodeH(n.right, path + "1", map);
    }

    /**
     * Decodes a stream of huffman codes from a file given as a stream of
     * bits into their uncompressed form, saving the results to the given
     * output stream. Note that the EOF character is not written to out
     * because it is not a valid 8-bit chunk (it is 9 bits).
     * 
     * @param in  the file to decompress.
     * @param out the file to write the decompressed output to.
     */
    public void decode(BitInputStream in, BitOutputStream out) {

        Node cur = root;
        while (true) {
            int bit = in.readBit();

            if (bit == 0) {
                cur = cur.left;
            } else {
                cur = cur.right;
            }

            if (cur.isLeaf()) {
                if (cur.value == 256) {
                    break; // We hit EOF;
                }
                out.writeBits(cur.value, 8);
                cur = root;
            }
        }
    }
}
