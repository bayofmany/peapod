package peapod.internal;

import com.tinkerpop.gremlin.structure.Edge;
import com.tinkerpop.gremlin.structure.Element;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import peapod.FramedEdge;
import peapod.FramedElement;
import peapod.FramedGraph;

public final class Knows$Impl extends Knows
        implements FramedEdge {
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

    private static final class Framer
            implements peapod.Framer<Edge, Knows> {

        private static final Framer instance = new Framer();
        private static final String label = "knows";
        private static final Collection<String> subLabels = Collections.unmodifiableCollection(Arrays.asList(label));

        public String label() {
            return label;
        }

        public Collection<String> subLabels() {
            return subLabels;
        }

        public Knows frame(Edge e, FramedGraph graph) {
            return new Knows$Impl(e, graph);
        }
    }

    public static peapod.Framer<Edge, Knows> framer() {
        return Framer.instance;
    }
}