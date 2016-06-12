package model;

import model.queries.Direction;
import model.queries.Pair;
import util.Hash;

import java.math.BigInteger;
import java.util.*;

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
     *
     * @param graph
     * @param nrOfSketches
     * @param nrOfBins
     */
    public GraphSummary(Graph graph, int nrOfSketches, int nrOfBins) {
        this(graph, nrOfBins);

        Random rnd = new Random();
        for (int i = 0; i < nrOfSketches; i++) {
            long seed = BigInteger.probablePrime(16, rnd).longValue();
            System.out.print("Creating sketch: " + i + "\t");
            createSketch(seed, i);
        }
        System.out.println();
    }

//    /**
//     * Creates a new graphsummary of the given graph containing a number of sketches which all have a given number of bins
//     * The specified seeds are used for each sketch. The number of generated sketches is equal to the number of provided seeds.
//     *
//     * @param graph
//     * @param nrOfBins
//     * @param seeds
//     */
//    public GraphSummary(Graph graph, int nrOfBins, long[] seeds) {
//        this(graph, nrOfBins);
//
//        for (int i = 0; i < seeds.length; i++) {
//            createSketch(seeds[i], i);
//        }
//    }
//

    /**
     * Creates an empty graph summary
     *
     * @param graph
     * @param nrOfBins
     */
    private GraphSummary(Graph graph, int nrOfBins) {
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

    public List<Pair<Integer, Integer>> getMergedWeightList(Direction direction) {
        List<Pair<Integer, Integer>> result = new ArrayList<>();
        Set<List<Pair<Integer, Integer>>> weightLists = new HashSet<>();

        for (GraphSketch sketch : graphSketches) {
            weightLists.add(sketch.getSortedWeights(direction));
        }

        for (int i = 0; i < nrOfBins; i++) {
            Pair<Integer, Integer> smallest = null;
            for (List<Pair<Integer, Integer>> weights : weightLists) {
                if (smallest == null || weights.get(i).getB() < smallest.getB()) {
                    smallest = weights.get(i);
                }
            }
            result.add(smallest);
        }
        return result;
    }

    /**
     * Creates a new GraphSketch and adds it to the set of sketches for this GraphSummary
     *
     * @param seed
     */
    public void createSketch(long seed, int index) {
        GraphSketch sketch = new GraphSketch(graph, new Hash(this.nrOfBins, seed, index));
        this.graphSketches.add(sketch);
    }
}
