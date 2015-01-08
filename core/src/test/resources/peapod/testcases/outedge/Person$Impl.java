package peapod.testcases.outedge;

import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.VertexProperty;
import peapod.FramedVertex;

public final class Person$Impl extends Person implements FramedVertex {

    private Vertex v;

    public Person$Impl(Vertex v) {
        this.v = v;
    }

    public Vertex vertex() {
        return v;
    }

    public java.util.List<Knows> getKnowsEdge() {
        return java.util.Collections.unmodifiableList(v.outE("knows").map(v -> (Knows) new Knows$Impl(v.get())).toList());
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