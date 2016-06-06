package model.queries;

import beaver.Symbol;
import model.GraphSummary;

/**
 * Abstract superclass for Queries that can be performed on a GraphSummary
 */
public abstract class GraphQuery extends Symbol {

    protected GraphSummary graphSummary;

    public GraphQuery(GraphSummary graphSummary) {
        this.graphSummary = graphSummary;
    }

    /**
     * Executes the query on the summarized graph and returns the result
     * @return
     */
    public abstract Object executeQueryOnSummary();

    /**
     * Executes the query on the original graph and returns the result.
     * @return
     */
    public abstract Object executeQueryOnOriginal();

    /**
     * Returns the minimum of the two specified input edge weights.
     * @param mergedWeight
     * @param currentWeight
     * @return The minimum weight as a Float or null if both input objects are null.
     */
    protected Integer mergeMinimum(Integer mergedWeight, Integer currentWeight) {
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

    public static boolean assertEquality(Object value1, Object value2) {
        System.out.println("value1: "+value1);
        System.out.println("value2: "+value2);
        if(value1==value2){
            return true;
        }
        if (value1 == null && value2 == null) {
            return true;
        }
        if (value1 == null || value2 == null) {
            return false;
        }
        return value1.equals(value2);
    }
}
