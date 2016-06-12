package model.queries;

import model.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

//            System.out.println("\t\t\t\t\t\t\t\t\t\taHash: "+aHash+"  bHash: "+bHash);
//            System.out.println("Weight for sketch: "+weight);
            mergedWeight = mergeMinimum(mergedWeight, weight);
        }

        return mergedWeight;
    }

    @Override
    public Object executeQueryOnOriginal() {
        Graph graph = super.graphSummary.getGraph();
        Node aVertex = graph.getVertices().get(this.labelA);
        Integer mergedWeight = null;

        if (aVertex == null) {
            return null;
        }

        for (Edge aOut : aVertex.getOutgoingEdges()) {
            Node bVertex = aOut.getTo();
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
            if (GraphQuery.assertEquality(summaryResult, originalResult)) {
                NrOfCorrectQueries++;
            }
        }

        return (float) NrOfCorrectQueries / (float) nrOfQueries;
    }


    /**
     * Returns the inter-accuracy of the sketch. It computes the top 100 edges from both original graph and the graph
     * sketch and then compute how much of them are overlapping. inter accuracy = countOfMatches / k
     *
     * @param graphSummary
     * @param k
     * @return InterAccuracy
     */
    public static float getInterAccuracy(GraphSummary graphSummary, int k) {

        Set<Edge> edgeSetOriginal = new HashSet<Edge>(graphSummary.getGraph().getEdges());
        List<Edge> edgeListOriginal = new ArrayList<Edge>();
        List<String> edgeIdOriginalList = new ArrayList<String>();

        for (Edge edge : edgeSetOriginal) {
            edgeListOriginal.add(edge);
        }

        Collections.sort(edgeListOriginal, Collections.reverseOrder());


        for (int i = 0; i < k; i++) {
            edgeIdOriginalList.add(edgeListOriginal.get(i).getFrom().getLabel() + "|" + edgeListOriginal.get(i).getTo().getLabel());
//            System.out.println(i + "th element weight original: " + edgeListOriginal.get(i).getWeight());
        }

        edgeListOriginal = null;
        System.gc();

        List<String> edgeIdSummaryList = new ArrayList<String>();

        Map<String, Integer> edgeTop100Pool = new HashMap<String, Integer>();

        int counter = 0;

        for (Edge edge : edgeSetOriginal) {
            counter += 1;
            if (counter % 1000000 == 0) {
                System.out.println(counter);
            }

            GraphQuery edgeQuery = new EdgeQuery(graphSummary, edge.getFrom().getLabel(), edge.getTo().getLabel());
            Integer weight = (Integer) edgeQuery.executeQueryOnSummary();

            String minKey = "";
            int minWeight = 1000000;
            if (edgeTop100Pool.size() >= k) {
                for (Map.Entry<String, Integer> edgeEntry : edgeTop100Pool.entrySet()) {
                    if (edgeEntry.getValue() < minWeight) {
                        minWeight = edgeEntry.getValue();
                        minKey = edgeEntry.getKey();
                    }
                }

                if (weight > minWeight) {
                    edgeTop100Pool.remove(minKey);
                    edgeTop100Pool.put(edge.getFrom().getLabel() + "|" + edge.getTo().getLabel(), weight);
                }
            } else {
                edgeTop100Pool.put(edge.getFrom().getLabel() + "|" + edge.getTo().getLabel(), weight);
            }

        }

        for (Map.Entry<String, Integer> edgeEntry : edgeTop100Pool.entrySet()) {
//            System.out.println(edgeEntry.getKey() + " " + edgeEntry.getValue());
            edgeIdSummaryList.add(edgeEntry.getKey());
        }


        for (int i = 0; i < k; i++) {
//            System.out.println(i + "th element weight sketch: " + edgeIdSummaryList.get(i));
        }

        int countOfMatches = 0;

        for (int a = 0; a < k; a++) {
            for (int b = 0; b < k; b++) {
                String edgeIdOriginal = edgeIdOriginalList.get(a);
                String edgeIdSummary = edgeIdSummaryList.get(b);
                if (edgeIdOriginal.equals(edgeIdSummary)) {
                    countOfMatches += 1;
                }
            }
        }
        return (float) countOfMatches / (float) k;

    }

    /**
     * Returns the average relative error from performing a given number of random Edge Queries on the specified graph summary and the original graph on which it is
     * based. The precision here is defined as relative error / (total nr of queries)  where relativeError = summaryResult / originalResult - 1.
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
            Node aVertex = graphSummary.getGraph().getVertices().get(a);
            Set<Edge> outGoingEdges = aVertex.getOutgoingEdges();

            Random randomizer = new Random();
            if (outGoingEdges.size() == 0) {
                i--;
                continue;
            }
            int item = randomizer.nextInt(outGoingEdges.size());
            int ii = 0;
            Edge theRandomEdge = null;
            for (Object obj : outGoingEdges) {
                if (ii == item)
                    theRandomEdge = (Edge) obj;
                ii = ii + 1;
            }

            Node bVertex = theRandomEdge.getTo();
            Integer originalWeight = theRandomEdge.getWeight();

            String b = bVertex.getLabel();

//            System.out.println(b);

            GraphQuery testQuery = new EdgeQuery(graphSummary, a, b);
            Integer summaryResult = (Integer) testQuery.executeQueryOnSummary();
            if (summaryResult == null)
                summaryResult = 0;
            Integer originalResult = (Integer) testQuery.executeQueryOnOriginal();

//            System.out.println(summaryResult);
//            System.out.println(originalWeight);
//            System.out.println(originalResult);
            Float relativeError = (float) summaryResult / (float) originalResult - 1;
            sumRelativeError += relativeError;

//            System.out.println("Query "+i+", relativeError: "+relativeError);
        }

        return sumRelativeError / (float) nrOfQueries;
    }

}
