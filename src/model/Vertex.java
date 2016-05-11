package model;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a single vertex (node) in a graph.
 */
public class Vertex {
    private String label;
    private Set<Edge> outgoingEdges;

    /**
     * Creates a new vertex with the specified identifier and label
     * @param label
     */
    public Vertex(String label) {
        this.label = label;
        this.outgoingEdges = new HashSet<>();
    }

    public String getLabel() {
        return label;
    }

    public Set<Edge> getOutgoingEdges() {
        return outgoingEdges;
    }

    public void addOutgoingEdgeTo(Edge outgoing) {
        this.outgoingEdges.add(outgoing);
    }
}
