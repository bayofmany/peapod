package peapod.testcases.outedge;

import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import peapod.FramedEdge;
import peapod.FramedGraph;

public final class Knows$Impl extends Knows {
    private FramedGraph graph;
    private Edge e;
    public Knows$Impl(Edge e, FramedGraph graph) {
        this.e = e;
        this.graph = graph;
    }
    public FramedGraph graph() {
        return graph;
    }
    public Element element() {
        return e;
    }
    public Edge edge() {
        return e;
    }
    public Person getPerson() {
        return e.outV().map(v -> new Person$Impl(v.get(), graph)).next();
    }
    public Person getOtherPerson() {
        return e.inV().map(v -> new Person$Impl(v.get(), graph)).next();
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