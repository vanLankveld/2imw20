package tests;

import model.Graph;
import model.GraphSketch;
import model.GraphSummary;
import model.queries.EdgeQuery;
import model.queries.NodeQuery;
import model.queries.PathQuery;
import model.queries.SubGraphQuery;
import util.Hash;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Test {

    public static void main(String[] args) {
        List<String> lines = new ArrayList<String>();
//      try (BufferedReader bufferedReader = new BufferedReader(new FileReader("test.csv"))) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("test_gt_graph"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

//      Graph graph = new Graph(lines, ",");
        Graph graph = new Graph(lines, " ","GT_GRAPH");
        System.out.println("Printing original graph: ");
        System.out.println(graph.toString());
        System.out.println();
        System.out.println("Creating summary...");
        GraphSummary summary = new GraphSummary(graph, 10, 50);

        /**
        int i = 0;
        for (GraphSketch sketch : summary.getGraphSketches()) {
            System.out.println(String.format("GraphSketch %s", i));
            System.out.println();
            System.out.println(sketch.toString());
            i++;
            System.out.println();
        }**/

        System.out.println(String.format("Precision of EdgeQuery: %.4f", EdgeQuery.getPrecision(summary, 1000)));
        System.out.println(String.format("Precision of NodeQuery: %.4f", NodeQuery.getPrecision(summary, 1000)));
        System.out.println(String.format("Precision of PathQuery: %.4f", PathQuery.getPrecision(summary, 1000)));
        System.out.println(String.format("Precision of SubGraphQuery: %.4f", SubGraphQuery.getPrecision(summary, 1000, 10)));
    }

}
