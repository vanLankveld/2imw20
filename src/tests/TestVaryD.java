package tests;

import model.Graph;
import model.GraphSummary;
import model.queries.EdgeQuery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestVaryD {

    public static void main(String[] args) {

        System.out.println("Reading file");
        List<String> lines = new ArrayList<String>();
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("test_dblp.csv"))) {
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("test.csv"))) {
        int count = 0;
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/Ming/Data/ipflow/equinix-chicago.dirA.20151217-125911.UTC.anon.txt"))) {
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("/Ming/Data/ipflow/equinix-chicago.dirA.20151217-125911.UTC.anon.sample.txt"))) {
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("dblp_co_authur.csv"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                count += 1;
                lines.add(line);
            }
            System.out.println(count + " edges read");

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Constructing graph");

        Graph graph = new Graph(lines, ",", "CSV");

        System.out.println("Graph built, num of edges: " + graph.getEdges().size() + ", num of nodes: " + graph.getVertices().size());

        lines = null;
        System.gc();
        System.out.println("Printing original graph: ");
//        System.out.println(graph.toString());
        System.out.println();
        System.out.println("Creating summary...");

        // For DBLP
        int[] nrSketchesExp = new int[]{1, 2, 3, 4, 5, 6, 7, 8, 9};
//        int[] nrBinsExp = new int[]{183, 196, 232, 259, 299, 366};
//        int[] nrBinsExp = new int[]{484};

        // For IP Flow
//        int[] nrBinsExp = new int[]{177, 184, 191, 200, 210, 221, 235, 251, 271};
//        int[] nrBinsExp = new int[]{191};

        for (int nrSketches : nrSketchesExp) {

            int nrBins = 191;
//            int nrBins = 0;

            GraphSummary summary = new GraphSummary(graph, nrSketches, nrBins);

            /**
             int i = 0;
             for (GraphSketch sketch : summary.getGraphSketches()) {
             System.out.println(String.format("GraphSketch %s", i));
             System.out.println();
             System.out.println(sketch.toString());
             i++;
             System.out.println();
             }**/

            System.out.println("=====================\nTEST RESULT:\nnrSketches: " + nrSketches + " nrBins: " + nrBins + "\n");
//            System.out.println(String.format("Inter Accuracy: %.4f", EdgeQuery.getInterAccuracy(summary, 100)));
            System.out.println(String.format("AverageRelativeError: %.4f", EdgeQuery.getAverageRelativeError(summary, 500)));

//        System.out.println(String.format("Precision of EdgeQuery: %.4f", EdgeQuery.getPrecision(summary, 1000)));
//        System.out.println(String.format("Precision of NodeQuery: %.4f", NodeQuery.getPrecision(summary, 1000)));
//        System.out.println(String.format("Precision of PathQuery: %.4f", PathQuery.getPrecision(summary, 1000)));
//        System.out.println(String.format("Precision of SubGraphQuery: %.4f", SubGraphQuery.getPrecision(summary, 1000, 10)));
        }
    }
}
