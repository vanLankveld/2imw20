package model.queries;

import model.*;

import java.util.*;

/**
 * Used to perform an aggregated edge query given two node labels. The specification for such a query
 * is given in Tang e.a. (2016). Graph Stream Summarization: From Big Bang to Big Crunch
 */
public class EdgeQuery extends GraphQuery {

    private String labelA, labelB;

    /**
     * Creates a new EdgeQuery instance which can be used to compute the aggregated edge weights between
     * two nodes with labels labelA and labelB for the specified GraphSummary.
     *
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
        Integer mergedWeight = null;

        for (GraphSketch graphSketch : super.graphSummary.getGraphSketches()) {
            int aHash = (int) graphSketch.getHash().hashToBin(this.labelA);
            int bHash = (int) graphSketch.getHash().hashToBin(this.labelB);

            Integer weight = graphSketch.getAdjMatrix()[aHash][bHash];

            mergedWeight = mergeMinimum(mergedWeight, weight);
        }

        return mergedWeight;
    }

    @Override
    public Object executeQueryOnOriginal() {
        Graph graph = super.graphSummary.getGraph();
        Vertex aVertex = graph.getVertices().get(this.labelA);
        Integer mergedWeight = null;

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
     *
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
            Integer summaryResult = (Integer) testQuery.executeQueryOnSummary();
            Integer originalResult = (Integer) testQuery.executeQueryOnOriginal();
            //System.out.println(String.format("Summary: %.4f; Original: %.4f", summaryResult, originalResult));
            if (GraphQuery.assertAquality(summaryResult, originalResult)) {
                NrOfCorrectQueries++;
            }
        }

        return (float) NrOfCorrectQueries / (float) nrOfQueries;
    }


    /**
     * !! Not complete yet
     * Returns the inter-accuracy of the sketch.
     *
     * @param graphSummary
     * @param k
     * @return
     */
    public static float getInterAccuracy(GraphSummary graphSummary, int k) {

        Set<Edge> edgeSetOriginal = new HashSet<Edge>(graphSummary.getGraph().getEdges());
        List<Edge> edgeListOriginal = new ArrayList<Edge>();

        for (Edge edge : edgeSetOriginal) {
            edgeListOriginal.add(edge);
        }

        Collections.sort(edgeListOriginal, Collections.reverseOrder());

        for (int i = 0; i < k; i++) {
            System.out.println(i + "th element weight original: " + edgeListOriginal.get(i).getWeight());
        }

        List<Edge> edgeListSummary = new ArrayList<Edge>();
        for (Edge edge : edgeSetOriginal) {
            GraphQuery edgeQuery = new EdgeQuery(graphSummary, edge.getFrom().getLabel(), edge.getTo().getLabel());
            Integer weight = (Integer) edgeQuery.executeQueryOnSummary();
            edgeListSummary.add(new Edge(new Vertex(edge.getFrom().getLabel()), new Vertex(edge.getTo().getLabel()), weight));
        }

        Collections.sort(edgeListSummary, Collections.reverseOrder());

        for (int i = 0; i < k; i++) {
            System.out.println(i + "th element weight sketch: " + edgeListSummary.get(i).getWeight());
        }

        int countOfMatches = 0;

        for (int a = 0; a < k; a++) {
            for (int b = 0; b < k; b++) {
                Edge edgeOriginal = edgeListOriginal.get(a);
                Edge edgeSummary = edgeListSummary.get(b);
                if (edgeOriginal.getFrom().getLabel().equals(edgeSummary.getFrom().getLabel()) &&
                        edgeOriginal.getTo().getLabel().equals(edgeSummary.getTo().getLabel())) {
                    countOfMatches += 1;
                }
            }
        }
        return (float) countOfMatches / (float) k;

    }


    /**
     * !! Not complete yet
     * Returns the precision from performing a given number of random Edge Queries on the specified graph summary and the original graph on which it is
     * based. If a query returns an equal result on both the graph summary and the original graph, the query result
     * is considered correct. The precision here is defined as (nr of correct queries) / (total nr of queries).
     *
     * @param graphSummary
     * @param nrOfQueries
     * @return
     */
    public static float getAverageRelativeError(GraphSummary graphSummary, int nrOfQueries) {
        int NrOfCorrectQueries = 0;

        List<String> labels = new ArrayList<>(graphSummary.getGraph().getVertices().keySet());

        Float sumRelativeError = 0f;

        for (int i = 0; i < nrOfQueries; i++) {
            Collections.shuffle(labels);
            String a = labels.get(0);
            String b = labels.get(1);

            GraphQuery testQuery = new EdgeQuery(graphSummary, a, b);
            Float summaryResult = (Float) testQuery.executeQueryOnSummary();
            Float originalResult = (Float) testQuery.executeQueryOnOriginal();
            Float relativeError = summaryResult / originalResult - 1;
            sumRelativeError+=relativeError;
        }

        return sumRelativeError / (float) nrOfQueries;
    }
}
