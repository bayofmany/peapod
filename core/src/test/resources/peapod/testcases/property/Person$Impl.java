package peapod.testcases.property;

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

    public String getName() {
        return v.<java.lang.String>property("name").orElse(null);
    }

    public Integer getAge() {
        return v.<java.lang.Integer>property("age").orElse(null);
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
