package model;

import util.Hash;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a single TCM graph sketch
 */
public class GraphSketch {

    private Integer[][] adjMatrix;
    private HashMap<Long, Set<String>> bins;
    private Hash hash;

    /**
     * Creates a new GraphSketch based on a given graph with the given hash function
     * @param graph
     * @param hash
     */
    public GraphSketch(Graph graph, Hash hash) {
        this.adjMatrix = new Integer[hash.getNrOfBins()][hash.getNrOfBins()];
        this.bins = new HashMap<>();
        this.hash = hash;
        this.constructSketch(graph);
    }

    public Integer[][] getAdjMatrix() {
        return adjMatrix;
    }

    public HashMap<Long, Set<String>> getBins() {
        return bins;
    }

    public Hash getHash() {
        return hash;
    }

    /**
     * Constructs the sketch of the given graph.
     * @param graph
     */
    private void constructSketch(Graph graph) {
        for (Edge edge : graph.getEdges()) {
            String labelFrom = edge.getFrom().getLabel();
            String labelTo = edge.getTo().getLabel();
            long binFrom = this.hash.hashToBin(labelFrom);
            long binTo = this.hash.hashToBin(labelTo);
            this.addToBin(binFrom, labelFrom);
            this.addToBin(binTo, labelTo);
            this.addToAdjMatrix((int)binFrom, (int)binTo, edge.getWeight());
        }
    }

    /**
     * Adds the specified label to the specified bin. If the bin does not exist, it is created.
     * @param bin
     * @param label
     */
    private void addToBin(long bin, String label) {
        if (this.bins.containsKey(bin)) {
            this.bins.get(bin).add(label);
            return;
        }
        Set<String> labels = new HashSet<>();
        labels.add(label);
        this.bins.put(bin, labels);
    }

    /**
     * Adds the specified weight to the specified edge in the adjacency matrix
     * @param from
     * @param to
     * @param weight
     */
    private void addToAdjMatrix(int from, int to, int weight) {
        if (this.adjMatrix[from][to] == null) {
            this.adjMatrix[from][to] = weight;
            return;
        }
        this.adjMatrix[from][to] += weight;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Bins: ");
        stringBuilder.append(System.lineSeparator());
        for (long bin : this.bins.keySet()) {
            stringBuilder.append(String.format("%s: {%s}", bin, String.join(",", this.bins.get(bin))));
            stringBuilder.append(System.lineSeparator());
        }

        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("Adjacency Matrix: ");
        stringBuilder.append(System.lineSeparator());

        for (long bin : this.bins.keySet()) {
            stringBuilder.append("\t\t");
            stringBuilder.append(bin);
        }
        stringBuilder.append(System.lineSeparator());

        for (int from = 0; from < this.hash.getNrOfBins(); from++){
            stringBuilder.append(from);
            stringBuilder.append("\t\t");
            for (int to = 0; to < this.hash.getNrOfBins(); to++) {
                if (this.adjMatrix[from][to] == null) stringBuilder.append("-");
                else stringBuilder.append(this.adjMatrix[from][to]);
                stringBuilder.append("\t\t");
            }
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }
}
