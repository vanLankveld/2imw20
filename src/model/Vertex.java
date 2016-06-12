package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents a single vertex (node) in a graph.
 */
public class Vertex {
    private String label;
    private Set<Edge> outgoingEdges;
    private int weightIn;
    private int weightOut;

    /**
     * Creates a new vertex with the specified identifier and label
     *
     * @param label
     */
    public Vertex(String label) {
        this.label = label;
        this.outgoingEdges = new HashSet<>();
        this.weightIn = 0;
        this.weightOut = 0;
    }

    public String getLabel() {
        return label;
    }

    public Set<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public int getWeightOut() {
        return weightOut;
    }

    public void setWeightOut(int weightOut) {
        this.weightOut = weightOut;
    }

    public int getWeightIn() {
        return weightIn;
    }

    public void setWeightIn(int weightIn) {
        this.weightIn = weightIn;
    }

    public void addOutgoingEdgeTo(Edge outgoing) {
        if (outgoingEdges.contains(outgoing)) {
            List<Edge> outgoingEdgesList = new ArrayList<Edge>(outgoingEdges);

            Edge existingEdge = outgoingEdgesList.get(outgoingEdgesList.indexOf(outgoing));
            existingEdge.addWeight(outgoing.getWeight());
            this.outgoingEdges.remove(outgoing);
            this.outgoingEdges.add(existingEdge);
        } else {
            this.outgoingEdges.add(outgoing);
        }
    }
}
