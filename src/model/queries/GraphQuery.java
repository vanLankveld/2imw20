package model.queries;

import model.GraphSummary;

/**
 * Created by Guus on 12-05-16.
 */
public abstract class GraphQuery {

    protected GraphSummary graphSummary;

    public GraphQuery(GraphSummary graphSummary) {
        this.graphSummary = graphSummary;
    }

    public abstract Object executeQueryOnSummary();

    public abstract Object executeQueryOnOriginal();
}
