package peapod.testcases.outvertex;

import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Vertex;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import peapod.FramedEdge;
import peapod.FramedElement;
import peapod.FramedGraph;
import peapod.FramedVertex;
import peapod.Framer;

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
        return Collections.unmodifiableList(v.out("knows").map(v -> (Person) new Person$Impl(v.get(), graph)).toList());
    }
    public void addKnows(Person person) {
        v.addEdge("knows", ((FramedVertex) person).vertex());
    }
    public int hashCode() {
        return v.hashCode();
    }
    public boolean equals(Object other) {
        return (other instanceof FramedElement) ? v.equals(((FramedElement) other).element()) : false;
    }
    public String toString() {
        return v.label() + "[" + v.id() + "]";
    }
    private static final class PersonFramer
            implements Framer<Person, Vertex> {
        private static final PersonFramer instance = new PersonFramer();
        private static final String label = "person";
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
    public static Framer<Person, Vertex> framer() {
        return PersonFramer.instance;
    }
}