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

public class Test {

    public static void main(String[] args) {

        System.out.println("Reading file");
        List<String> lines = new ArrayList<String>();
//        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("test_dblp.csv"))) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("dblp_co_authur.csv"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("Constructing graph");

        Graph graph = new Graph(lines, ",", "CSV");
        lines = null;
        System.gc();
        System.out.println("Printing original graph: ");
//        System.out.println(graph.toString());
        System.out.println();
        System.out.println("Creating summary...");

        int[] nrBinsExp = new int[]{183, 196, 232, 259, 299, 366};

        for (int nrBins : nrBinsExp) {

            int nrSketches = 9;
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
            System.out.println(String.format("Inter Accuracy: %.4f", EdgeQuery.getInterAccuracy(summary, 100)));
            System.out.println(String.format("AverageRelativeError: %.4f", EdgeQuery.getAverageRelativeError(summary, 500)));

//        System.out.println(String.format("Precision of EdgeQuery: %.4f", EdgeQuery.getPrecision(summary, 1000)));
//        System.out.println(String.format("Precision of NodeQuery: %.4f", NodeQuery.getPrecision(summary, 1000)));
//        System.out.println(String.format("Precision of PathQuery: %.4f", PathQuery.getPrecision(summary, 1000)));
//        System.out.println(String.format("Precision of SubGraphQuery: %.4f", SubGraphQuery.getPrecision(summary, 1000, 10)));
        }
    }
}
