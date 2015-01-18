package peapod.testcases.outvertex;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import peapod.FramedEdge;
import peapod.FramedGraph;
import peapod.FramedVertex;

import java.util.Collections;

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

    public java.util.List<Person> getKnows() {
        return Collections.unmodifiableList(v.out("knows").map(v -> (peapod.testcases.outvertex.Person) new peapod.testcases.outvertex.Person$Impl(v.get(), graph)).toList());
    }

    public void addKnows(Person person) {
        v.addEdge("knows", ((FramedVertex) person).vertex());
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
