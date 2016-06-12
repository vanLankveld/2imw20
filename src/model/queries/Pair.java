package model.queries;

import beaver.Symbol;

public class Pair<T extends Comparable, S extends Comparable> extends Symbol {
    private T a;
    private S b;

    public Pair(T a, S b) {
        this.a = a;
        this.b = b;
    }

    public T getA() {
        return a;
    }

    public S getB() {
        return b;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        Pair<?,?> pair = (Pair<?,?>) o;

        if (a != null ? !a.equals(pair.a) : pair.a != null) return false;
        return b != null ? b.equals(pair.b) : pair.b == null;

    }

    @Override
    public int hashCode() {
        int result = a != null ? a.hashCode() : 0;
        result = 31 * result + (b != null ? b.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s)", this.a, this.b);
    }
}