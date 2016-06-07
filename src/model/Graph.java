package model;

import java.util.*;

/**
 * Represents a graph consisting of a set of edges and vertices. Also uses an adjacency list for each vertex.
 */
public class Graph {

    private Set<Edge> edges;
    private Map<String, Vertex> vertices;

    /**
     * Create a new graph based on a list of Strings, each representing a single edge.
     * Each edge-String should have the following format: fromVertex,toVertex,weight.
     *
     * @param edges
     */
    public Graph(Collection<String> edges) {
        this(edges, ",");
    }

    /**
     * Create a new graph based on a list of Strings, each representing a single edge.
     * Each edge-String should have the following format: fromVertex,toVertex,weight. With ',' being an arbitrary delimiter which can be specified as an argument
     *
     * @param edges
     * @param delimiter
     */
    public Graph(Collection<String> edges, String delimiter) {
        this(edges, delimiter, "CSV");
    }

    /**
     * Create a new graph based on a list of Strings, each representing a single edge.
     * For CSV format, each edge-String should have the following format: fromVertex,toVertex,weight. With ',' being an arbitrary delimiter which can be specified as an argument
     * For GT_GRAPH format, only lines starting with 'a' are considered as edge-String, they should have the following format: a fromVertex toVertex weight. With ' '(space) being an arbitrary delimiter which can be specified as an argument
     *
     * @param edges
     * @param delimiter
     * @param format
     */
    public Graph(Collection<String> edges, String delimiter, String format) {
        this.edges = new HashSet<>();
        this.vertices = new HashMap<>();

        if (format.equals("CSV")) {

            for (String s : edges) {
                String[] split = s.split(delimiter);
                if (split.length < 3) {
                    throw new IllegalArgumentException(String.format("Input data should be in the form of 'from %s to $s weight' (without quotes)", delimiter, delimiter));
                }

                String fromLabel = split[0];
                String toLabel = split[1];
                int weight = Integer.parseInt(split[2]);

                Vertex from = this.getVertexByIdOrCreate(fromLabel);
                Vertex to = this.getVertexByIdOrCreate(toLabel);
                Edge edge = new Edge(from, to, weight);
                from.addOutgoingEdgeTo(edge);

//Added reverse for undirected graph
//                Edge edge_rev = new Edge(to, from, weight);
//                to.addOutgoingEdgeTo(edge_rev);

                this.edges.add(edge);
            }
        } else if (format.equals("GT_GRAPH")) {
            for (String s : edges) {
                if (s.startsWith("a ")) {
                    String[] split = s.split(delimiter);

                    if (split.length < 4) {
                        throw new IllegalArgumentException(String.format("Input data should be in the form of 'a from %s to $s weight' (without quotes)", delimiter, delimiter));
                    }

                    String fromLabel = split[1];
                    String toLabel = split[2];
                    int weight = Integer.parseInt(split[3]);

                    Vertex from = this.getVertexByIdOrCreate(fromLabel);
                    Vertex to = this.getVertexByIdOrCreate(toLabel);
                    Edge edge = new Edge(from, to, weight);
                    from.addOutgoingEdgeTo(edge);
                    this.edges.add(edge);
                }
            }

        } else
            throw new IllegalArgumentException(String.format("Format %s is not supported, currently only CSV and GT_GRAPH are supported"));

    }

    public Set<Edge> getEdges() {
        return edges;
    }

    public Map<String, Vertex> getVertices() {
        return vertices;
    }

    /**
     * If a vertex with the specified id exists in the set of vertices for this graph: returns this vertex. Otherwise,
     * creates a new vertex, adds it to the set of vertices and returns it.
     *
     * @param label
     * @return
     */
    private Vertex getVertexByIdOrCreate(String label) {
        if (this.vertices.containsKey(label)) {
            return vertices.get(label);
        }
        Vertex vertex = new Vertex(label);
        this.vertices.put(vertex.getLabel(), vertex);
        return vertex;
    }

    @Override
    public String toString() {
        StringBuilder outerSB = new StringBuilder();

        outerSB.append("Graph as adjacency list: ");
        outerSB.append(System.lineSeparator());
        for (Vertex vertex : this.vertices.values()) {
            StringBuilder innerSB = new StringBuilder();
            innerSB.append(vertex.getLabel());
            innerSB.append(" {");
            Set<String> listElements = new HashSet<>();
            for (Edge edge : vertex.getOutgoingEdges()) {
                listElements.add(String.format("(%s, %s)", edge.getTo().getLabel(), edge.getWeight()));
            }
            innerSB.append(String.join(", ", listElements));
            innerSB.append("}");
            outerSB.append(innerSB.toString());
            outerSB.append(System.lineSeparator());
        }
        return outerSB.toString();
    }
}
