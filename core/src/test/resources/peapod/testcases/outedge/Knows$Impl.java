package peapod.testcases.outedge;

import com.tinkerpop.gremlin.structure.Edge;
import peapod.FramedEdge;

public final class Knows$Impl extends Knows {
    private Edge e;

    public Knows$Impl(Edge e) {
        this.e = e;
    }

    public Edge edge() {
        return e;
    }

    public Person getPerson() {
        return e.outV().map(v -> new Person$Impl(v.get())).next();
    }

    public Person getOtherPerson() {
        return e.inV().map(v -> new Person$Impl(v.get())).next();
    }

    public int hashCode() {
        return e.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof FramedEdge) ? e.equals(((FramedEdge) other).edge()) : false;
    }

    public String toString() {
        return e.label() + "[" + e.id() + "]";
    }
}