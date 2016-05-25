package model;

import beaver.Symbol;
import model.queries.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SubGraph extends Symbol {
    private Set<Pair<String>> edges;

    public SubGraph(Set<Pair<String>> edges) {
        this.edges = edges;
    }

    public SubGraph() {
        this(new HashSet<>());
    }

    public Set<Pair<String>> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        List<String> edgesStringList = new ArrayList<>();
        for (Pair<String> edge : this.edges) {
            edgesStringList.add(edge.toString());
        }
        String edgesString = String.join(",", edgesStringList);
        return String.format("{%s}", edgesString);
    }
}
