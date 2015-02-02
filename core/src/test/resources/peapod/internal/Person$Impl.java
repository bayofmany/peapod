package peapod.internal;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import peapod.FramedElement;
import peapod.FramedGraph;
import peapod.FramedVertex;
import peapod.internal.runtime.Framer;
import peapod.internal.runtime.IFramer;
import com.tinkerpop.gremlin.structure.Edge;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public final class Person$Impl extends Person
        implements FramedVertex<Person> {

    private FramedGraph graph;
    private Vertex v;
    public Person$Impl(Vertex v, FramedGraph graph) {
        this.v  = v;
        this.graph = graph;
    }
    public FramedGraph graph() {
        return graph;
    }
    public Element element() {
        return v;
    }
    public String getName() {
        return v.<String>property("name").orElse(null);
    }
    public List<Knows> getKnows() {
        return Collections.unmodifiableList(v.outE("knows").map(it -> graph.<Knows, Edge>frame(it.get())).toList());
    }
    public int hashCode() {
        return v.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof FramedElement) && v.equals(((FramedElement) other).element());
    }

    public String toString() {
        return v.label() + "[" + v.id() + "]";
    }

    @Framer
    public static final class PersonFramer
            implements IFramer<Vertex, Person> {

        public Class<Vertex> type() {
            return Vertex.class;
        }

        public Class<Person> frameClass() {
            return peapod.internal.Person.class;
        }

        public String label() {
            return "Person";
        }

        public Person frame(Vertex v, FramedGraph graph) {
            return new Person$Impl(v, graph);
        }

        public Person frameNew(Vertex v, FramedGraph graph) {
            return frame(v, graph);
        }
    }
}