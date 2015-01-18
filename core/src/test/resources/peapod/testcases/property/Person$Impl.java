package peapod.testcases.property;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import peapod.FramedGraph;
import peapod.FramedVertex;

public final class Person$Impl extends Person
        implements FramedVertex {
    private FramedGraph graph;
    private Vertex v;
    public Person$Impl(Vertex v, FramedGraph graph) {
        this.v = v;
        this.graph = graph;
    }
    public FramedGraph graph() {
        return graph;
    }
    public Element element() {
        return v;
    }
    public Vertex vertex() {
        return v;
    }
    public String getName() {
        return v.<String>property("name").orElse(null);
    }
    public Integer getAge() {
        return v.<Integer>property("age").orElse(null);
    }
    public int hashCode() {
        return v.hashCode();
    }
    public boolean equals(Object other) {
        return (other instanceof FramedVertex) ? v.equals(((FramedVertex) other).vertex()) : false;
    }
    public String toString() {
        return v.label() + "[" + v.id() + "]";
    }
}