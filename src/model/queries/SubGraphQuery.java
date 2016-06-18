package model.queries;

import model.*;

import java.util.*;

/**
 * Used to perform an aggregated subgraph query given two node labels. The specification for such a query
 * is given in Tang e.a. (2016). Graph Stream Summarization: From Big Bang to Big Crunch
 */
public class SubGraphQuery extends GraphQuery {

    private SubGraph subGraph;

    public SubGraphQuery(GraphSummary graphSummary, SubGraph subGraph) {
        super(graphSummary);
        this.subGraph = subGraph;
    }

    @Override
    public Object executeQueryOnSummary() {
        Integer mergedWeight = null;

        for (GraphSketch sketch : this.graphSummary.getGraphSketches()) {
            Integer currentWeight = null;
            for (Pair<String, String> pair : this.subGraph.getEdges()) {
                int hashedA = (int)sketch.getHash().hashToBin(pair.getA());
                int hashedB = (int)sketch.getHash().hashToBin(pair.getB());

                Integer weight = sketch.getAdjMatrix()[hashedA][hashedB];

                if (weight != null) {
                    if  (currentWeight == null) {
                        currentWeight = weight;
                    }
                    else {
                        currentWeight += weight;
                    }
                }
            }
            mergedWeight = mergeMinimum(mergedWeight, currentWeight);
        }

        if (mergedWeight == null) {
            return 0;
        }
        return mergedWeight;
    }

    @Override
    public Object executeQueryOnOriginal() {
        Integer weight = 0;

        for (Pair<String, String> pair : this.subGraph.getEdges()) {

            Node vertexA = this.graphSummary.getGraph().getVertices().get(pair.getA());

            for (Edge edge : vertexA.getOutgoingEdges()) {
                if (edge.getTo().getLabel().equals(pair.getB())) {
                    weight += edge.getWeight();
                }
            }
        }

        return weight;
    }

    public static float getPrecision(GraphSummary graphSummary, int nrOfQueries, int subGraphUpperBound) {
        int NrOfCorrectQueries = 0;

        List<String> labels = new ArrayList<>(graphSummary.getGraph().getVertices().keySet());

        Random random = new Random();

        for (int i = 0; i < nrOfQueries; i++) {

            SubGraph subGraph = new SubGraph();

            int subGraphSize = 1 + random.nextInt(subGraphUpperBound-1);
            for (int j = 0; j < subGraphSize; j++) {
                Collections.shuffle(labels);
                String a = labels.get(0);
                String b = labels.get(1);
                subGraph.getEdges().add(new Pair<>(a, b));
            }

            GraphQuery testQuery = new SubGraphQuery(graphSummary, subGraph);
            Integer summaryResult = (Integer)testQuery.executeQueryOnSummary();
            Integer originalResult = (Integer)testQuery.executeQueryOnOriginal();
            //System.out.println(String.format("Summary: %.4f; Original: %.4f", summaryResult, originalResult));
            if (GraphQuery.assertEquality(summaryResult, originalResult)) {
                NrOfCorrectQueries++;
            }
        }

        return (float)NrOfCorrectQueries / (float)nrOfQueries;
    }

    public static float getAverageRelativeError(GraphSummary graphSummary, int nrOfQueries) {
        Random random = new Random();

        Float sumRelativeError = 0f;

        for (int i = 0; i < nrOfQueries; i++) {
            SubGraph subGraph = generateSubGraph(graphSummary.getGraph(), random.nextInt(7)+2);

            GraphQuery query = new SubGraphQuery(graphSummary, subGraph);

            Integer summarizedResult = (Integer)query.executeQueryOnSummary();
            Integer originalResult = (Integer)query.executeQueryOnOriginal();

            sumRelativeError += (((float)summarizedResult / (float)originalResult) - 1);
        }

        return sumRelativeError / (float)nrOfQueries;
    }

    public static SubGraph generateSubGraph(Graph graph, int nrOfEdges) {
        Set<Edge> connectedEdges = new HashSet<>();

        List<Edge> edges = new ArrayList<>(graph.getEdges());
        Collections.shuffle(edges);

        Set<Edge> visited = new HashSet<>();

        int i = 0;
        Edge start = edges.get(i);
        while (start.getTo().getOutgoingEdges().size() < 1
                && start.getFrom().getOutgoingEdges().size() < 2) {
            i++;
            start = edges.get(i);
        }

        getConnectedEdges(start, visited, connectedEdges, nrOfEdges);

        SubGraph subGraph = new SubGraph();

        for (Edge edge : connectedEdges) {
            subGraph.addEdge(edge);
        }

        return subGraph;
    }

    private static void getConnectedEdges(Edge current, Set<Edge> visited, Set<Edge> connected, int nrOfEdges) {
        if (connected.size() >= nrOfEdges || visited.contains(current)) {
            return;
        }

        visited.add(current);
        connected.add(current);

        Set<Edge> connectedToCurrent = new HashSet<>(current.getFrom().getOutgoingEdges());
        connectedToCurrent.addAll(current.getTo().getOutgoingEdges());

        for (Edge edge : connectedToCurrent) {
            getConnectedEdges(edge, visited, connected, nrOfEdges);
        }
    }
}
