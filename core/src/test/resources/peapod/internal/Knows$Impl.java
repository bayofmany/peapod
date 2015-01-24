package peapod.internal;

import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import peapod.FramedEdge;
import peapod.FramedElement;
import peapod.FramedGraph;
import peapod.internal.IFramer;

public final class Knows$Impl extends Knows
        implements FramedEdge {

    public static final String LABEL = "knows";

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
        return (other instanceof FramedElement) && e.equals(((FramedElement) other).element());
    }

    public String toString() {
        return e.label() + "[" + e.id() + "]";
    }

    public static final Framer instance = new Framer();

    private static final class Framer
            implements IFramer<Edge, Knows> {

        private static final Collection<String> subLabels = Collections.unmodifiableCollection(Arrays.asList(LABEL));

        public String label() {
            return LABEL;
        }

        public Collection<String> subLabels() {
            return subLabels;
        }

        public Knows frame(Edge e, FramedGraph graph) {
            return new Knows$Impl(e, graph);
        }
    }
}