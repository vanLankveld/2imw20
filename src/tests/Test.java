package tests;

import com.sun.xml.internal.fastinfoset.util.StringArray;
import model.Graph;
import model.GraphSketch;
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
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader("test.csv"))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lines.add(line);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Graph graph = new Graph(lines, ",");
        System.out.println("Printing original graph: ");
        System.out.println(graph.toString());
        System.out.println();
        System.out.println("Creating sketch...");
        System.out.println();
        GraphSketch sketch = new GraphSketch(graph, new Hash(8, 37));
        System.out.println("Printing graph sketch:");
        System.out.println(sketch.toString());
    }

}
