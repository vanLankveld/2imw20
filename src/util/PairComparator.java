package util;

import model.queries.Pair;

import java.util.Comparator;

/**
 * Created by Guus on 07-06-16.
 */
public class PairComparator implements Comparator {

    public enum CompareOn {
        LEFT, RIGHT
    }

    private CompareOn compareOn;
    private SortOrder sortOrder;

    public PairComparator(CompareOn compareOn, SortOrder sortOrder) {
        this.compareOn = compareOn;
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(Object o1, Object o2) {
        if (!(o1 instanceof Pair && o2 instanceof Pair)) {
            return 0;
        }

        Pair p1 = (Pair)o1;
        Pair p2 = (Pair)o2;

        int returnValue = 0;

        if (compareOn == CompareOn.LEFT) {
            returnValue = p1.getA().compareTo(p2.getA());
        }
        returnValue = p1.getB().compareTo(p2.getB());

        if (this.sortOrder == SortOrder.NORMAL) {
            return returnValue;
        }
        return -returnValue;
    }
}
