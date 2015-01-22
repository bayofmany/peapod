package peapod.impl;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import peapod.FramedElement;
import peapod.FramedGraph;
import peapod.FramedVertex;

public final class Person$Impl extends Person
        implements FramedVertex<Person> {
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
    public java.util.List<Knows> getKnows() {
        return Collections.unmodifiableList(v.outE("knows").map(v -> (peapod.impl.Knows) new peapod.impl.Knows$Impl(v.get(), graph)).toList());
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

    private static final class Framer
            implements peapod.Framer<Vertex, Person> {

        private static final Framer instance = new Framer();
        private static final String label = "Person";
        private static final Collection<String> subLabels = Collections.unmodifiableCollection(Arrays.asList(label));

        public String label() {
            return label;
        }

        public Collection<String> subLabels() {
            return subLabels;
        }

        public Person frame(Vertex v, FramedGraph graph) {
            return new Person$Impl(v, graph);
        }
    }

    public static peapod.Framer<Vertex, Person> framer() {
        return Framer.instance;
    }
}