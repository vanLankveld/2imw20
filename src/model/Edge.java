package model;

/**
 * Represents a single directed edge in a graph
 */
public class Edge {
    private Vertex from;
    private Vertex to;
    private float weight;

    /**
     * Creates a new directed edge between to vertices with direction from->to
     * @param from
     * @param to
     * @param weight
     */
    public Edge(Vertex from, Vertex to, float weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    /**
     * Gets the source vertex
     * @return
     */
    public Vertex getFrom() {
        return from;
    }

    /**
     * Gets the destination vertex
     * @return
     */
    public Vertex getTo() {
        return to;
    }

    /**
     * Gets the weight of this edge
     * @return
     */
    public float getWeight() {
        return weight;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Edge)) return false;

        Edge edge = (Edge) o;

        if (Float.compare(edge.weight, weight) != 0) return false;
        if (!from.equals(edge.from)) return false;
        return to.equals(edge.to);

    }

    @Override
    public int hashCode() {
        int result = from.hashCode();
        result = 31 * result + to.hashCode();
        result = 31 * result + (weight != +0.0f ? Float.floatToIntBits(weight) : 0);
        return result;
    }
}
