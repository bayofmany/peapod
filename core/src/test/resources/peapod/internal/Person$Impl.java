package peapod.internal;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import peapod.FramedElement;
import peapod.FramedGraph;
import peapod.FramedVertex;
import peapod.internal.IFramer;
import java.util.List;

public final class Person$Impl extends Person
        implements FramedVertex<Person> {

    public static final String LABEL = "Person";

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
    public String getName() {
        return v.<String>property("name").orElse(null);
    }
    public List<Knows> getKnows() {
        return Collections.unmodifiableList(v.outE("knows").map(v -> (peapod.internal.Knows) new peapod.internal.Knows$Impl(v.get(), graph)).toList());
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

    public static final Framer instance = new Framer();

    private static final class Framer
            implements IFramer<Vertex, Person> {

        private static final Collection<String> subLabels = Collections.unmodifiableCollection(Arrays.asList(LABEL));

        public String label() {
            return LABEL;
        }

        public Collection<String> subLabels() {
            return subLabels;
        }

        public Person frame(Vertex v, FramedGraph graph) {
            return new Person$Impl(v, graph);
        }
    }
}
