package util;

/**
 * Class to construct a hash finunction to use with the graph sketch
 */
public class Hash {
    private static final int DEFAULT_SEED = 5381; //Default value used as initial hash value
    private long seed; //Initial hash value
    private int nrOfBins; //Nr of possible bins to map to

    /**
     * Creates a hash function using a given base value and a given nr of output bins.
     * @param seed
     * @param nrOfBins
     */
    public Hash(int nrOfBins, long seed) {
        this.seed = seed;
        this.nrOfBins = nrOfBins;
    }

    /**
     * Creates a hash function with a given nr of output bins using the default base value.
     * @param nrOfBins
     */
    public Hash(int nrOfBins) {
        this(DEFAULT_SEED, nrOfBins);
    }

    public int getNrOfBins() {
        return nrOfBins;
    }

    public long getInitHash() {
        return seed;
    }

    /**
     * Maps the given value to a bin
     * @param value
     * @return
     */
    public long hashToBin(String value) {
        long hash = (long)seed;
        for (int i = 0; i < value.length(); i++) {
            hash = ((hash << 5) + hash) + value.charAt(i);
        }
        return hash % nrOfBins;
    }
}
