package model.queries;

import model.*;

import javax.management.Query;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Guus on 12-05-16.
 */
public class EdgeQuery extends GraphQuery {

    private String labelA, labelB;

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

    private Float mergeMinimum(Float mergedWeight, Float currentWeight) {
        if (currentWeight == null) {
            return mergedWeight;
        }
        if (mergedWeight == null || mergedWeight > currentWeight) {
           return currentWeight;
        }
        else {
            return mergedWeight;
        }
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

    public static float getPrecision(GraphSummary graphSummary, int nrOfQueries) {
        int NrOfCorrectQueries = 0;

        List<String> labels = new ArrayList<>(graphSummary.getGraph().getVertices().keySet());

        for (int i = 0; i < nrOfQueries; i++) {
            Collections.shuffle(labels);
            String a = labels.get(0);
            String b = labels.get(1);

            GraphQuery testQuery = new EdgeQuery(graphSummary, a, b);
            if (testQuery.executeQueryOnSummary() != testQuery.executeQueryOnOriginal()) {
                NrOfCorrectQueries++;
            }
        }

        return (float)NrOfCorrectQueries / (float)nrOfQueries;
    }
}
