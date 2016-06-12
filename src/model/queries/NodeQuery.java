package model.queries;

import model.Edge;
import model.GraphSketch;
import model.GraphSummary;
import model.Node;
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
        Integer currentWeight;

        Integer currentBucket = null;

        for (GraphSketch graphSketch : this.graphSummary.getGraphSketches()) {
            int bucketIndex = (int)graphSketch.getHash().hashToBin(this.nodeLabel);
            currentWeight = 0;
            for (int i = 0; i < graphSketch.getAdjMatrix()[0].length; i++) {
                switch (this.direction.getDirection()) {
                    case OUT:
                        if (graphSketch.getAdjMatrix()[bucketIndex][i] == null) {
                            continue;
                        }
                        currentWeight += graphSketch.getAdjMatrix()[bucketIndex][i];
                        break;
                    case IN:
                        if (graphSketch.getAdjMatrix()[i][bucketIndex] == null) {
                            continue;
                        }
                        currentWeight += graphSketch.getAdjMatrix()[i][bucketIndex];
                        break;
                    case UNDIRECTED:
                        if (graphSketch.getAdjMatrix()[bucketIndex][i] != null) {
                            currentWeight += graphSketch.getAdjMatrix()[bucketIndex][i];
                        }
                        if (graphSketch.getAdjMatrix()[i][bucketIndex] != null) {
                            currentWeight += graphSketch.getAdjMatrix()[i][bucketIndex];
                        }
                        break;
                }
            }
            Integer oldWeight = mergedWeight;
            mergedWeight = this.mergeMinimum(mergedWeight, currentWeight);
            if (!mergedWeight.equals(oldWeight)) {
                currentBucket = bucketIndex;
            }
        }
        return new Pair<>(currentBucket, mergedWeight);
    }

    @Override
    public Object executeQueryOnOriginal() {

        Integer mergedWeight = null;

        for (Edge edge : this.graphSummary.getGraph().getEdges()) {

            String to = edge.getTo().getLabel();
            String from = edge.getFrom().getLabel();

            switch (this.direction.getDirection()) {
                case IN:
                    if (to != this.nodeLabel) {
                        continue;
                    }
                    break;
                case OUT:
                    if (from != this.nodeLabel) {
                        continue;
                    }
                    break;
                default:
                    if (from != this.nodeLabel && to != this.nodeLabel) {
                        continue;
                    }
                    break;
            }
            mergedWeight = this.mergeMinimum(mergedWeight, edge.getWeight());
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


        return 0;
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
