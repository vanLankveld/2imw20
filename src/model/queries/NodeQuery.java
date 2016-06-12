package model.queries;

import model.*;
import util.PairComparator;
import util.SortOrder;
import util.VertexComparator;

import javax.jnlp.IntegrationService;
import java.util.*;

/**
 * Used to perform an aggregated node query given a single node label and a direction. The specification for such a query
 * is given in Tang e.a. (2016). Graph Stream Summarization: From Big Bang to Big Crunch
 */
public class NodeQuery extends GraphQuery {

    private final String nodeLabel;
    private final Direction direction;

    /**
     * Creates a new NodeQuery instance which can be used to compute the aggregated edge weights to and/or from
     * two node with label nodeLabel for the specified GraphSummary.
     * @param graphSummary
     * @param nodeLabel
     * @param direction
     */
    public NodeQuery(GraphSummary graphSummary, String nodeLabel, Direction direction) {
        super(graphSummary);
        this.nodeLabel = nodeLabel;
        this.direction = direction;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public Object executeQueryOnSummary() {

        Integer mergedWeight = null;
        Integer currentWeight = 0;

        Integer currentBucket = null;

        for (GraphSketch sketch : this.graphSummary.getGraphSketches()) {
            Integer summedWeightOut = 0;
            Integer summedWeightIn = 0;
            Integer bucket = (int)sketch.getHash().hashToBin(nodeLabel);

            if (this.direction.getDirection() == Direction.DirectionEnum.OUT ||
                    this.direction.getDirection() == Direction.DirectionEnum.UNDIRECTED) {
                for (int i = 0; i < this.graphSummary.getNrOfBins(); i++) {
                    Integer weight = sketch.getAdjMatrix()[bucket][i];
                    if (weight != null) {
                        summedWeightOut += weight;
                    }
                }
                currentWeight += summedWeightOut;
            }
            if (this.direction.getDirection() == Direction.DirectionEnum.IN ||
                    this.direction.getDirection() == Direction.DirectionEnum.UNDIRECTED) {
                for (int i = 0; i < this.graphSummary.getNrOfBins(); i++) {
                    Integer weight = sketch.getAdjMatrix()[i][bucket];
                    if (weight != null) {
                        summedWeightIn += weight;
                    }
                }
                currentWeight += summedWeightIn;
            }

            mergedWeight = this.mergeMinimum(mergedWeight, currentWeight);
            if (mergedWeight != currentWeight) {
                currentBucket = bucket;
            }
        }

        if (mergedWeight == null) {
            mergedWeight = 0;
        }

        return new Pair<>(currentBucket, mergedWeight);
    }

    @Override
    public Object executeQueryOnOriginal() {

        Integer mergedWeight = 0;

        for (Edge edge : this.graphSummary.getGraph().getEdges()) {

            String to = edge.getTo().getLabel();
            String from = edge.getFrom().getLabel();

            switch (this.direction.getDirection()) {
                case IN:
                    if (!to.equals(this.nodeLabel)) {
                        continue;
                    }
                    break;
                case OUT:
                    if (!from.equals(this.nodeLabel)) {
                        continue;
                    }
                    break;
                case UNDIRECTED:
                    if (!from.equals(this.nodeLabel) && !to.equals(this.nodeLabel)) {
                        continue;
                    }
                    break;
            }
            mergedWeight += edge.getWeight();
        }

        if (mergedWeight == null) {
            mergedWeight = 0;
        }

        return mergedWeight;
    }

    /**
     * Returns the precision from performing a given number of random Node Queries on the specified graph summary and the original graph on which it is
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
            String nodeLabel = labels.get(0);

            Direction d = Direction.random();

            GraphQuery testQuery = new NodeQuery(graphSummary, nodeLabel, d);
            Float summaryResult = (Float)testQuery.executeQueryOnSummary();
            Float originalResult = (Float)testQuery.executeQueryOnOriginal();
            //System.out.println(String.format("Summary: %.4f; Original: %.4f", summaryResult, originalResult));
            if (GraphQuery.assertEquality(summaryResult, originalResult)) {
                NrOfCorrectQueries++;
            }
        }

        return (float)NrOfCorrectQueries / (float)nrOfQueries;
    }

    public static float getAverageRelativeError(GraphSummary graphSummary, int nrOfQueries, Direction direction) {
        List<Edge> edges = new ArrayList<>(graphSummary.getGraph().getEdges());

        Float sumRelativeError = 0f;

        for (int i = 0; i < nrOfQueries; i++) {
            Collections.shuffle(edges);
            Edge edge = edges.get(0);
            String label;

            if (direction.getDirection() == Direction.DirectionEnum.IN) {
                label = edge.getTo().getLabel();
            }
            else if (direction.getDirection() == Direction.DirectionEnum.OUT) {
                label = edge.getFrom().getLabel();
            }
            else {
                if (Math.random() > 0.5) {
                    label = edge.getFrom().getLabel();
                }
                else {
                    label = edge.getTo().getLabel();
                }
            }

            GraphQuery query = new NodeQuery(graphSummary, label, direction);

            Integer summarizedResult = ((Pair<Integer, Integer>)query.executeQueryOnSummary()).getB();
            Integer originalResult = (Integer)query.executeQueryOnOriginal();

            sumRelativeError += (((float)summarizedResult / (float)originalResult) - 1);
        }

        return sumRelativeError / (float)nrOfQueries;
    }

    public static float getInterAccuracy(GraphSummary graphSummary, int k, Direction direction) {

        List<Node> nodeListOriginal = new ArrayList<>(graphSummary.getGraph().getVertices().values());
        List<Integer> topKOriginal = new ArrayList<>();


        Collections.sort(nodeListOriginal, new VertexComparator(direction, SortOrder.REVERSE));

        for (int i = 0; i < k; i++) {
            Node n = nodeListOriginal.get(i);

            GraphQuery query = new NodeQuery(graphSummary, n.getLabel(), direction);
            Pair<Integer, Integer> queryResult = (Pair<Integer, Integer>)query.executeQueryOnSummary();
            topKOriginal.add(queryResult.getA());
            int weight = 0;
            switch (direction.getDirection()) {
                case IN:
                    weight = n.getWeightIn();
                    break;
                case OUT:
                    weight = n.getWeightOut();
                    break;
                case UNDIRECTED:
                    weight = n.getWeightIn() + n.getWeightOut();
                    break;
            }
            System.out.println(i + "th element weight original: " + weight);
        }

        System.gc();

        List<Pair<Integer, Integer>> summarizedWeights = graphSummary.getMergedWeightList(direction);
        Collections.sort(summarizedWeights, new PairComparator(PairComparator.CompareOn.RIGHT, SortOrder.REVERSE));

        List<Integer> topKSummarized = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            topKSummarized.add(summarizedWeights.get(i).getA());
        }

        System.gc();

        topKSummarized.retainAll(topKOriginal);

        return (float)topKSummarized.size() / (float)k;
    }

}
