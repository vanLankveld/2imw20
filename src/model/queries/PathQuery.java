package model.queries;

import model.*;

import java.util.*;

/**
 * Used to perform an aggregated node query given two node labels. The specification for such a query
 * is given in Tang e.a. (2016). Graph Stream Summarization: From Big Bang to Big Crunch
 */
public class PathQuery extends GraphQuery {

    private String labelA, labelB;

    /**
     * Creates a new PathQuery instance which can be used to determine whether a path exists between
     * two nodes with labels labelA and labelB for the specified GraphSummary.
     * @param graphSummary
     * @param labelA
     * @param labelB
     */
    public PathQuery(GraphSummary graphSummary, String labelA, String labelB) {
        super(graphSummary);
        this.labelA = labelA;
        this.labelB = labelB;
    }

    @Override
    public Object executeQueryOnSummary() {
        for (GraphSketch sketch : this.graphSummary.getGraphSketches()) {
            int binA = (int)sketch.getHash().hashToBin(labelA);
            int binB = (int)sketch.getHash().hashToBin(labelB);
            if (!reach(binA, binB, new HashSet<>(), sketch)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object executeQueryOnOriginal() {
        return reach(labelA, labelB, new HashSet<String>(), this.graphSummary.getGraph());
    }

    /**
     * Uses a simple breadth-first search to determine whether node with labelB is reachable from labelB in the given
     * GraphSketch
     * @param binA
     * @param binB
     * @param visited
     * @param sketch
     * @return
     */
    private static boolean reach(int binA, int binB, Set<Integer> visited, GraphSketch sketch) {
        if (!visited.contains(binA)) {
            visited.add(binA);
        }

        if (binA == binB) {
            return true;
        }

        for (int i = 0; i < sketch.getAdjMatrix()[binA].length; i++) {
            Float weightToNeighbour =  sketch.getAdjMatrix()[binA][i];
            if (weightToNeighbour == null || visited.contains(i)) {
                continue;
            }

            if (reach(i, binB, visited, sketch)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Uses a simple breadth-first search to determine whether node with labelB is reachable from labelB in the given
     * Graph
     * @param labelA
     * @param labelB
     * @param visited
     * @param graph
     * @return
     */
    private static boolean reach(String labelA, String labelB, Set<String> visited, Graph graph) {
        visited.add(labelA);

        if (labelA.equals(labelB)) {
            return true;
        }

        for (Edge edgeOut : graph.getVertices().get(labelA).getOutgoingEdges()) {
            if (visited.contains(edgeOut.getTo().getLabel())) {
                continue;
            }

            if (reach(edgeOut.getTo().getLabel(), labelB, visited, graph)) {
                return true;
            }
        }

        return false;
    }


    public static float getPrecision(GraphSummary graphSummary, int nrOfQueries) {
        int NrOfCorrectQueries = 0;

        List<String> labels = new ArrayList<>(graphSummary.getGraph().getVertices().keySet());

        for (int i = 0; i < nrOfQueries; i++) {
            Collections.shuffle(labels);
            String a = labels.get(0);
            String b = labels.get(1);

            GraphQuery testQuery = new PathQuery(graphSummary, a, b);
            Boolean summaryResult = (Boolean)testQuery.executeQueryOnSummary();
            Boolean originalResult = (Boolean)testQuery.executeQueryOnOriginal();
            //System.out.println(String.format("Summary: %.4f; Original: %.4f", summaryResult, originalResult));
            if (GraphQuery.assertAquality(summaryResult, originalResult)) {
                NrOfCorrectQueries++;
            }
        }

        return (float)NrOfCorrectQueries / (float)nrOfQueries;
    }
}
