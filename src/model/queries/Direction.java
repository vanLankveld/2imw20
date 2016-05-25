package model.queries;

import beaver.Symbol;

/**
 * Created by Guus on 25-05-16.
 */
public class Direction extends Symbol {
    public enum DirectionEnum {
        IN, OUT, UNDIRECTED;
    }

    private DirectionEnum direction;

    public Direction(DirectionEnum direction) {
        this.direction = direction;
    }

    public DirectionEnum getDirection() {
        return direction;
    }

    public static Direction random() {
        return new Direction(DirectionEnum.values()[(int)((double)DirectionEnum.values().length * Math.random())]);
    }

    @Override
    public String toString() {
        return this.direction.name();
    }
}
