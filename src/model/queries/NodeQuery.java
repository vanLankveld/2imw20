package model.queries;

import model.Edge;
import model.GraphSketch;
import model.GraphSummary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Used to perform an aggregated node query given two node labels. The specification for such a query
 * is given in Tang e.a. (2016). Graph Stream Summarization: From Big Bang to Big Crunch
 */
public class NodeQuery extends GraphQuery {

    public enum Direction {
        IN, OUT, UNDIRECTED;

        public static Direction random() {
            return Direction.values()[(int)((double)Direction.values().length * Math.random())];
        }
    }

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

        Float mergedWeight = null;
        Float currentWeight;

        for (GraphSketch graphSketch : this.graphSummary.getGraphSketches()) {
            int bucketIndex = (int)graphSketch.getHash().hashToBin(this.nodeLabel);

            for (int i = 0; i < graphSketch.getAdjMatrix()[0].length; i++) {
                switch (this.direction) {
                    case OUT:
                        currentWeight = graphSketch.getAdjMatrix()[bucketIndex][i];
                        break;
                    case IN:
                        currentWeight = graphSketch.getAdjMatrix()[i][bucketIndex];
                        break;
                    default:
                        if (graphSketch.getAdjMatrix()[bucketIndex][i] == null) {
                            currentWeight = graphSketch.getAdjMatrix()[i][bucketIndex];
                        }
                        else if (graphSketch.getAdjMatrix()[i][bucketIndex] == null) {
                            currentWeight = graphSketch.getAdjMatrix()[i][bucketIndex];
                        }
                        else {
                            currentWeight = Math.min(graphSketch.getAdjMatrix()[bucketIndex][i], graphSketch.getAdjMatrix()[i][bucketIndex]);
                        }
                        break;
                }
                mergedWeight = this.mergeMinimum(mergedWeight, currentWeight);
            }
        }
        return mergedWeight;
    }

    @Override
    public Object executeQueryOnOriginal() {

        Float mergedWeight = null;

        for (Edge edge : this.graphSummary.getGraph().getEdges()) {

            String to = edge.getTo().getLabel();
            String from = edge.getFrom().getLabel();

            switch (this.direction) {
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
            if (GraphQuery.assertAquality(summaryResult, originalResult)) {
                NrOfCorrectQueries++;
            }
        }

        return (float)NrOfCorrectQueries / (float)nrOfQueries;
    }

}
