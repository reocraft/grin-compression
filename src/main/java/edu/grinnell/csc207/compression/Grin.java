package edu.grinnell.csc207.compression;

import java.util.Map;
import java.util.HashMap;

/**
 * The driver for the Grin compression program.
 */
public class Grin {

    public static final int MAGIC_NUM = 0x736;

    /**
     * Decodes the .grin file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * 
     * @param infile  the file to decode
     * @param outfile the file to ouptut to
     * @throws Exception
     */
    public static void decode(String infile, String outfile) throws Exception {
        BitInputStream input = null;
        BitOutputStream output = null;

        try {
            input = new BitInputStream(infile);
            int magicCode = input.readBits(32);
            if (magicCode != MAGIC_NUM) {
                throw new Exception(infile + " is not a valid Grin file.");
            }
            HuffmanTree tree = new HuffmanTree(input);
            output = new BitOutputStream(outfile);
            tree.decode(input, output);

        } catch (Exception e) {
            throw new Exception("We faced an exception in decoding.", e);
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * Creates a mapping from 8-bit sequences to number-of-occurrences of
     * those sequences in the given file. To do this, read the file using a
     * BitInputStream, consuming 8 bits at a time.
     * 
     * @param file the file to read
     * @return a freqency map for the given file
     * @throws Exception
     */
    public static Map<Short, Integer> createFrequencyMap(String file) throws Exception {
        Map<Short, Integer> freqMap = new HashMap<>();
        BitInputStream input = null;
        try {
            input = new BitInputStream(file);
            while (true) {
                short bits = (short) input.readBits(8);
                if (bits == -1) {
                    break;
                }
                int count;
                if (freqMap.containsKey(bits)) {
                    count = freqMap.get(bits) + 1;
                } else {
                    count = 1;
                }
                freqMap.put(bits, count);
            }

        } catch (Exception e) {
            throw new Exception("We faced an exception in creating the frequency map.", e);
        } finally {
            if (input != null) {
                input.close();
            }
        }
        return freqMap;
    }

    /**
     * Encodes the given file denoted by infile and writes the output to the
     * .grin file denoted by outfile.
     * 
     * @param infile  the file to encode.
     * @param outfile the file to write the output to.
     * @throws Exception
     */
    public static void encode(String infile, String outfile) throws Exception {
        BitInputStream input = null;
        BitOutputStream output = null;
        String inputFile = infile;
        String outputFile = outfile;

        try {
            Map<Short, Integer> freqMap = createFrequencyMap(inputFile);
            HuffmanTree tree = new HuffmanTree(freqMap);

            output = new BitOutputStream(outputFile);
            output.writeBits(MAGIC_NUM, 32);
            tree.serialize(output);

            input = new BitInputStream(inputFile);
            tree.encode(input, output);

        } catch (Exception e) {
            throw new Exception("We faced an exception in encoding.", e);
        } finally {
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
        }
    }

    /**
     * The entry point to the program.
     * 
     * @param args the command-line arguments.
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        if (args.length != 3) {
            System.out.println("Usage: java Grin <encode|decode> <infile> <outfile>");
            return;
        }

        String code = args[0];
        String infile = args[1];
        String outfile = args[2];

        if (code.equals("encode")) {
            encode(infile, outfile);
        } else if (code.equals("decode")) {
            decode(infile, outfile);
        } else {
            System.out.println("Usage: java Grin <encode|decode> <infile> <outfile>");
        }

    }
}
