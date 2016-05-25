package model.queries;

import model.Edge;
import model.GraphSketch;
import model.GraphSummary;
import model.Vertex;

import java.util.*;

/**
 * Used to perform an aggregated subgraph query given two node labels. The specification for such a query
 * is given in Tang e.a. (2016). Graph Stream Summarization: From Big Bang to Big Crunch
 */
public class SubGraphQuery extends GraphQuery {



    Set<Pair<String>> subGraph;

    public SubGraphQuery(GraphSummary graphSummary, Set<Pair<String>> subGraph) {
        super(graphSummary);
        this.subGraph = subGraph;
    }

    @Override
    public Object executeQueryOnSummary() {
        Integer mergedWeight = 0;

        for (GraphSketch sketch : this.graphSummary.getGraphSketches()) {
            Integer currentWeight = 0;
            for (Pair<String> pair : this.subGraph) {

                int hashedA = (int)sketch.getHash().hashToBin(pair.getA());
                int hashedB = (int)sketch.getHash().hashToBin(pair.getB());

                Integer weight = sketch.getAdjMatrix()[hashedA][hashedB];

                if (weight != null) {
                    currentWeight += weight;
                }
            }
            mergedWeight = mergeMinimum(mergedWeight, currentWeight);
        }

        if (mergedWeight == 0f) {
            return null;
        }
        return mergedWeight;
    }

    @Override
    public Object executeQueryOnOriginal() {
        Integer weight = 0;

        for (Pair<String> pair : this.subGraph) {

            Vertex vertexA = this.graphSummary.getGraph().getVertices().get(pair.getA());

            for (Edge edge : vertexA.getOutgoingEdges()) {
                if (edge.getTo().getLabel().equals(pair.getB())) {
                    weight += edge.getWeight();
                }
            }
        }

        if (weight == 0f) {
            return null;
        }
        return weight;
    }

    public static float getPrecision(GraphSummary graphSummary, int nrOfQueries, int subGraphUpperBound) {
        int NrOfCorrectQueries = 0;

        List<String> labels = new ArrayList<>(graphSummary.getGraph().getVertices().keySet());

        Random random = new Random();

        for (int i = 0; i < nrOfQueries; i++) {

            Set<Pair<String>> subGraph = new HashSet<>();

            int subGraphSize = 1 + random.nextInt(subGraphUpperBound-1);
            for (int j = 0; j < subGraphSize; j++) {
                Collections.shuffle(labels);
                String a = labels.get(0);
                String b = labels.get(1);
                subGraph.add(new Pair<>(a, b));
            }

            GraphQuery testQuery = new SubGraphQuery(graphSummary, subGraph);
            Integer summaryResult = (Integer)testQuery.executeQueryOnSummary();
            Integer originalResult = (Integer)testQuery.executeQueryOnOriginal();
            //System.out.println(String.format("Summary: %.4f; Original: %.4f", summaryResult, originalResult));
            if (GraphQuery.assertAquality(summaryResult, originalResult)) {
                NrOfCorrectQueries++;
            }
        }

        return (float)NrOfCorrectQueries / (float)nrOfQueries;
    }
}
