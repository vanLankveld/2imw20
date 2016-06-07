package util;

import java.io.UnsupportedEncodingException;
import java.util.Random;

/**
 * Class to construct a hash finunction to use with the graph sketch
 */
public class Hash {
    public static final long PRIME_MODULUS = (1L << 31) - 1;
    private static final int DEFAULT_SEED = 5381; //Default value used as initial hash value
    private long seed; //Initial hash value
    private int nrOfBins; //Nr of possible bins to map to
    private int index;
    private int[] hashRand;
    private long LONG_PRIME = 32993;
    private int RAND_MAX = 32767;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Creates a hash function using a given base value and a given nr of output bins.
     *
     * @param seed
     * @param nrOfBins
     */
    public Hash(int nrOfBins, long seed, int index) {
        this.seed = seed;
        this.nrOfBins = nrOfBins;
        this.index = index;
        this.hashRand = new int[2];
        this.hashRand[0] = (int) ((float) (new Random().nextInt(RAND_MAX)) * (float) (LONG_PRIME) / (float) (RAND_MAX) + 1);
        this.hashRand[1] = (int) ((float) (new Random().nextInt(RAND_MAX)) * (float) (LONG_PRIME) / (float) (RAND_MAX) + 1);
    }

//    /**
//     * Creates a hash function with a given nr of output bins using the default base value.
//     *
//     * @param nrOfBins
//     */
//    public Hash(int nrOfBins) {
//        this(DEFAULT_SEED, nrOfBins, 0);
//    }

    public int getNrOfBins() {
        return nrOfBins;
    }

    public long getInitHash() {
        return seed;
    }

    /**
     * Maps the given value to a bin
     *
     * @param value
     * @return
     */
    public long hashToBin_old(String value) {
        long hash = (long) seed;
        for (int i = 0; i < value.length(); i++) {
            hash = ((hash << 5) + hash) + value.charAt(i);
        }
        return hash % nrOfBins;
    }

    public long hashstr(String str) {
        long hash = (long) 5381;
        for (int i = 0; i < str.length(); i++) {
            hash = ((hash << 5) + hash) + str.charAt(i);
        }
        return hash;
    }

    //Using hash in c++ countmin
//    public long hashToBin(String str) {
//        long hashval = Long.parseLong(str);
//        long hashBin = (hashRand[0] * hashval + hashRand[1]) % nrOfBins;
//        return hashBin;
//    }

//    int hash(long item, int i) {
//        long hash = hashRand[0] * item;
//        // A super fast way of computing x mod 2^p-1
//        // See http://www.cs.princeton.edu/courses/archive/fall09/cos521/Handouts/universalclasses.pdf
//        // page 149, right after Proposition 7.
//        hash += hash >> 32;
//        hash &= PRIME_MODULUS;
//        // Doing "%" after (int) conversion is ~2x faster than %'ing longs.
//        return ((int) hash) % nrOfBins;
//    }


    //Using Murmur hashing
    public long hashToBin(String str) {
        return getHashBuckets(str,index,nrOfBins);
    }

    // Murmur is faster than an SHA-based approach and provides as-good collision
    // resistance.  The combinatorial generation approach described in
    // http://www.eecs.harvard.edu/~kirsch/pubs/bbbf/esa06.pdf
    // does prove to work in actual tests, and is obviously faster
    // than performing further iterations of murmur.
    public static int getHashBuckets(String key, int hashCount, int max) {
        byte[] b;
        try {
            b = key.getBytes("UTF-16");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return getHashBuckets(b, hashCount, max);
    }

    static int getHashBuckets(byte[] b, int index, int max) {
        int hash1 = MurmurHash.hash(b, b.length, 0);
        int hash2 = MurmurHash.hash(b, b.length, hash1);
        return Math.abs((hash1 + index * hash2) % max);
    }

}
