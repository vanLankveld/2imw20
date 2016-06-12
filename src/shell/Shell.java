package shell;

import beaver.Parser;
import model.Graph;
import model.GraphSketch;
import model.GraphSummary;
import model.SubGraph;
import model.queries.*;
import shell.parser.TCMQueryParser;
import shell.parser.TCMQueryScanner;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Used to run a commandline utility to test the system in real-time
 */
public class Shell {

    public static GraphSummary graphSummary;

    public static void CreateGraphSummary(String filename, int numberOfSketches, int nrOfBins, String graphtype, String delimiter) {
        output(String.format("Creating Graph summary from file %s with %s sketches and %s bins...", filename, numberOfSketches, nrOfBins));
        List<String> lines = new ArrayList<String>();
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Graph graph = new Graph(lines, delimiter, graphtype);
        lines = null;
        System.gc();
        graphSummary = new GraphSummary(graph, numberOfSketches, nrOfBins);
        System.gc();
        output("Done");
    }

    public static void executeQuery(GraphQuery query) {
        output("Executing query");
        Object result = query.executeQueryOnSummary();
        output("Result: " + result.toString());
    }

    public static GraphQuery createEdgeQuery(String a, String b) {
        output(String.format("Creating edge query for edge ('%s', '%s')...", a, b));
        return new EdgeQuery(graphSummary, a, b);
    }

    public static GraphQuery createNodeQuery(String l, Direction d) {
        output(String.format("Creating edge query for node '%s' with direction: '%s')...", l, d.toString()));
        return new NodeQuery(graphSummary, l , d);
    }

    public static GraphQuery createPathQuery(String a, String b) {
        output(String.format("Creating path query for a path from '%s' to '%s')...", a, b));
        return new PathQuery(graphSummary, a, b);
    }

    public static GraphQuery createSubGraphQuery(SubGraph g) {
        output(String.format("Creating subgraph query with subgraph %s...", g.toString()));
        return new SubGraphQuery(graphSummary, g);
    }

    public static void BenchmarkEdgeQuery(int n) {
        output(String.format("Benchmarking EdgeQuery with number of queries=%s", n));
        output(String.format("Inter accuracy of EdgeQuery: %.4f", EdgeQuery.getInterAccuracy(graphSummary, n)));
        output(String.format("Average relative error of EdgeQuery: %.4f", EdgeQuery.getAverageRelativeError(graphSummary, n)));
    }

    public static void BenchmarkNodeQuery(int n) {
        output(String.format("Benchmarking NodeQuery with number of queries=%s", n));
        output(String.format("Inter accuracy of NodeQuery: %.4f", NodeQuery.getInterAccuracy(graphSummary, n, new Direction(Direction.DirectionEnum.UNDIRECTED))));
        output(String.format("Average relative error of NodeQuery: %.4f", NodeQuery.getAverageRelativeError(graphSummary, n, new Direction(Direction.DirectionEnum.UNDIRECTED))));
    }

    public static void BenchmarkPathQuery(int n) {
        output(String.format("Benchmarking PathQuery with number of queries=%s", n));
        output(String.format("Inter accuracy of PathQuery: %.4f", PathQuery.getInterAccuracy(graphSummary, n)));
        output(String.format("Average relative error of PathQuery: %.4f", PathQuery.getAverageRelativeError(graphSummary, n)));
    }

    public static void showSummary() {
        int i = 0;
        for (GraphSketch sketch : graphSummary.getGraphSketches()) {
            System.out.println(String.format("GraphSketch %s", i));
            System.out.println();
            System.out.println(sketch.toString());
            i++;
            System.out.println();
        }
    }

    public static void BenchmarkSubGraphQuery(int n, int u) {
        output(String.format("Benchmarking SubGraphQuery with number of queries=%s", n));
        output(String.format("Inter accuracy of SubGraphQuery: %.4f", SubGraphQuery.getInterAccuracy(graphSummary, n)));
        output(String.format("Average relative error of SubGraphQuery: %.4f", SubGraphQuery.getAverageRelativeError(graphSummary, n)));
    }

    public static void main(String[] args) throws IOException, Parser.Exception {
        boolean retry = true;
        while(retry) {
            retry = false;
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

                TCMQueryScanner scanner = new TCMQueryScanner(reader);

                TCMQueryParser parser = new TCMQueryParser();

                parser.parse(scanner);
            } catch (Error e) {
                error("Syntax error. Please try again");
                retry = true;
            }
        }
    }

    public static void output(String output) {
        output(output, false);
    }

    public static void error(String output) {
        output(output, true);
    }

    private static void output(String output, boolean error) {
        String[] outputLines = output.split(System.lineSeparator());

        if (error) {
            System.err.println(">> Error: ");
        }

        for (String line : outputLines) {
            if (error) {
                System.err.println(">> " + line);
                continue;
            }
            System.out.println(">> " + line);
        }
    }
}
