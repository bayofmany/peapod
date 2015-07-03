package peapod.internal.interfaces;

import java.util.Iterator;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import peapod.FramedElement;
import peapod.FramedGraph;
import peapod.FramedVertex;
import peapod.internal.runtime.FrameHelper;
import peapod.internal.runtime.Framer;
import peapod.internal.runtime.IFramer;
import java.util.Collections;
import java.util.List;
import org.apache.tinkerpop.gremlin.structure.Edge;

@SuppressWarnings("unused")
public final class Programmer$Impl
        implements Programmer, Person, FramedVertex<Programmer> {

    private FramedGraph graph;
    private Vertex v;
    public Programmer$Impl(Vertex v, FramedGraph graph) {
        this.v  = v;
        this.graph = graph;
    }
    public FramedGraph graph() {
        return graph;
    }
    public Element element() {
        return v;
    }
    public void setExperience(Integer years) {
        if (years == null) {
            v.property("experience").remove();
        } else {
            v.property("experience", years);
        }
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
    public static final class ProgrammerFramer
            implements IFramer<Vertex, Programmer> {

        public Class<Vertex> type() {
            return Vertex.class;
        }

        public Class<Programmer> frameClass() {
            return Programmer.class;
        }

        public String label() {
            return "Programmer";
        }

        public Programmer frame(Vertex v, FramedGraph graph) {
            return new Programmer$Impl(v, graph);
        }

        public Programmer frameNew(Vertex v, FramedGraph graph) {
            return frame(v, graph);
        }
    }
}