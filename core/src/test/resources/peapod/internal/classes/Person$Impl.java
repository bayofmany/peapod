package peapod.internal.classes;

import java.lang.Class;
import java.lang.Object;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import peapod.FramedElement;
import peapod.FramedGraph;
import peapod.FramedVertex;
import peapod.internal.runtime.Framer;
import peapod.internal.runtime.IFramer;


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
        // getter-edge-collection
        return graph.frame(v.edges(Direction.OUT, "knows"), Knows.class);
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
            return Person.class;
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