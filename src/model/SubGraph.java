package model;

import beaver.Symbol;
import model.queries.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubGraph extends Symbol {
    private Set<Pair<String, String>> edges;

    public SubGraph(Set<Pair<String, String>> edges) {
        this.edges = edges;
    }

    public SubGraph() {
        this(new HashSet<>());
    }

    public Set<Pair<String, String>> getEdges() {
        return edges;
    }

    public void addEdge(Edge edge) {
        Pair<String, String> subGraphEdge = new Pair<>(edge.getFrom().getLabel(), edge.getTo().getLabel());
        this.edges.add(subGraphEdge);
    }

    @Override
    public String toString() {
        List<String> edgesStringList = new ArrayList<>();
        for (Pair<String, String> edge : this.edges) {
            edgesStringList.add(edge.toString());
        }
        String edgesString = String.join(",", edgesStringList);
        return String.format("{%s}", edgesString);
    }
}
