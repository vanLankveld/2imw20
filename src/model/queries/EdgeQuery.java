package model.queries;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to perform an aggregated edge query given two node labels. The specification for such a query
 * is given in Tang e.a. (2016). Graph Stream Summarization: From Big Bang to Big Crunch
 */
public class EdgeQuery extends GraphQuery {

    private String labelA, labelB;

    /**
     * Creates a new EdgeQuery instance which can be used to compute the aggregated edge weights between
     * two nodes with labels labelA and labelB for the specified GraphSummary.
     * @param graphSummary
     * @param labelA
     * @param labelB
     */
    public EdgeQuery(GraphSummary graphSummary, String labelA, String labelB) {
        super(graphSummary);
        this.labelA = labelA;
        this.labelB = labelB;
    }

    @Override
    public Object executeQueryOnSummary() {
        Float mergedWeight = null;

        for (GraphSketch graphSketch : super.graphSummary.getGraphSketches()) {
            int aHash = (int)graphSketch.getHash().hashToBin(this.labelA);
            int bHash = (int)graphSketch.getHash().hashToBin(this.labelB);

            Float weight = graphSketch.getAdjMatrix()[aHash][bHash];

            mergedWeight = mergeMinimum(mergedWeight, weight);
        }

        return mergedWeight;
    }

    @Override
    public Object executeQueryOnOriginal() {
        Graph graph = super.graphSummary.getGraph();
        Vertex aVertex = graph.getVertices().get(this.labelA);
        Float mergedWeight = null;

        if (aVertex == null) {
            return null;
        }

        for (Edge aOut : aVertex.getOutgoingEdges()) {
            Vertex bVertex = aOut.getTo();
            if (this.labelB.equals(bVertex.getLabel())) {
                mergedWeight = mergeMinimum(mergedWeight, aOut.getWeight());
            }
        }

        return mergedWeight;
    }

    /**
     * Returns the precision from performing a given number of random Edge Queries on the specified graph summary and the original graph on which it is
     * based. If a query returns an equal result on both the graph summary and the original graph, the query result
     * is considered correct. The precision here is defined as (nr of correct queries) / (total nr of queries).
     * @param graphSummary
     * @param nrOfQueries
     * @return
     */
    public static float getPrecision(GraphSummary graphSummary, int nrOfQueries) {
        int NrOfCorrectQueries = 0;

        List<String> labels = new ArrayList<>(graphSummary.getGraph().getVertices().keySet());

        for (int i = 0; i < nrOfQueries; i++) {
            Collections.shuffle(labels);
            String a = labels.get(0);
            String b = labels.get(1);

            GraphQuery testQuery = new EdgeQuery(graphSummary, a, b);
            Float summaryResult = (Float)testQuery.executeQueryOnSummary();
            Float originalResult = (Float)testQuery.executeQueryOnOriginal();
            //System.out.println(String.format("Summary: %.4f; Original: %.4f", summaryResult, originalResult));
            if (GraphQuery.assertEquality(summaryResult, originalResult)) {
                NrOfCorrectQueries++;
            }
        }

        return (float)NrOfCorrectQueries / (float)nrOfQueries;
    }
}
