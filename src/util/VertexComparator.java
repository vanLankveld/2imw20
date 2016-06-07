package util;

import model.Vertex;
import model.queries.Direction;

import java.util.Collections;
import java.util.Comparator;

/**
 * Created by Guus on 06-06-16.
 */
public class VertexComparator implements Comparator {

    private Direction direction;
    private SortOrder sortOrder;

    public VertexComparator(Direction direction, SortOrder sortOrder) {
        this.direction = direction;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Vertex && o2 instanceof Vertex))
        {
            return 0;
        }

        Vertex v1 = (Vertex)o1;
        Vertex v2 = (Vertex)o2;

        int returnValue = 0;

        switch (this.direction.getDirection()) {
            case IN:
                returnValue = v1.getWeightIn() - v2.getWeightOut();
                break;
            case OUT:
                returnValue = v1.getWeightOut() - v2.getWeightOut();
                break;
            case UNDIRECTED:
                returnValue = (v1.getWeightOut() + v1.getWeightIn()) - (v2.getWeightOut() + v2.getWeightIn());
                break;
        }

        if (this.sortOrder == SortOrder.NORMAL) {
            return returnValue;
        }
        return -returnValue;
    }
}
