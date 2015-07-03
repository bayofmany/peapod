package peapod.internal.classes;

import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Element;
import peapod.FramedEdge;
import peapod.FramedElement;
import peapod.FramedGraph;
import peapod.internal.runtime.Framer;
import peapod.internal.runtime.IFramer;

@SuppressWarnings("unused")
public final class Knows$Impl extends Knows
        implements FramedEdge {

    private FramedGraph graph;
    private Edge e;
    public Knows$Impl(Edge e, FramedGraph graph) {
        this.e  = e;
        this.graph = graph;
    }
    public FramedGraph graph() {
        return graph;
    }
    public Element element() {
        return e;
    }
    public Person getPerson() {
        // edge-getter-vertex
        return graph().frame(e.outVertex(), Person.class);
    }
    public Person getOtherPerson() {
        // edge-getter-vertex
        return graph().frame(e.inVertex(), Person.class);
    }
    public int hashCode() {
        return e.hashCode();
    }

    public boolean equals(Object other) {
        return (other instanceof FramedElement) && e.equals(((FramedElement) other).element());
    }

    public String toString() {
        return e.label() + "[" + e.id() + "]";
    }

    @Framer
    public static final class KnowsFramer
            implements IFramer<Edge, Knows> {

        public Class<Edge> type() {
            return Edge.class;
        }

        public Class<Knows> frameClass() {
            return Knows.class;
        }

        public String label() {
            return "knows";
        }

        public Knows frame(Edge e, FramedGraph graph) {
            return new Knows$Impl(e, graph);
        }

        public Knows frameNew(Edge e, FramedGraph graph) {
            return frame(e, graph);
        }
    }
}