package model;

import util.Hash;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Contains a number of graphsketches
 */
public class GraphSummary {

    private Set<GraphSketch> graphSketches;
    private Graph graph;
    private int nrOfBins;

    /**
     * Creates a new graphsummary of the given graph containing a given nr of sketches which all have a given number of bins
     * Each sketch uses a randomly generated hash seed
     * @param graph
     * @param nrOfSketches
     * @param nrOfBins
     */
    public GraphSummary(Graph graph, int nrOfSketches, int nrOfBins) {
        this(graph, nrOfBins);

        Random rnd = new Random();

        for (int i = 0; i < nrOfSketches; i++) {
            long seed = BigInteger.probablePrime(24, rnd).longValue();
            System.out.println("Creating sketch: " + i+", seed: "+seed);
            createSketch(seed);
        }
    }

    /**
     * Creates a new graphsummary of the given graph containing a number of sketches which all have a given number of bins
     * The specified seeds are used for each sketch. The number of generated sketches is equal to the number of provided seeds.
     * @param graph
     * @param nrOfBins
     * @param seeds
     */
    public GraphSummary(Graph graph, int nrOfBins, long[] seeds) {
        this(graph, nrOfBins);

        for (int i = 0; i < seeds.length; i++) {
            createSketch(seeds[i]);
        }
    }

    /**
     * Creates an empty graph summary
     * @param graph
     * @param nrOfBins
     */
    private GraphSummary(Graph graph,  int nrOfBins) {
        this.graph = graph;
        this.nrOfBins = nrOfBins;
        this.graphSketches = new HashSet<>();
    }

    public Set<GraphSketch> getGraphSketches() {
        return graphSketches;
    }

    public Graph getGraph() {
        return graph;
    }

    public int getNrOfBins() {
        return nrOfBins;
    }

    /**
     * Creates a new GraphSketch and adds it to the set of sketches for this GraphSummary
     * @param seed
     */
    public void createSketch(long seed) {
        GraphSketch sketch = new GraphSketch(graph, new Hash(this.nrOfBins, seed));
        this.graphSketches.add(sketch);
    }
}
