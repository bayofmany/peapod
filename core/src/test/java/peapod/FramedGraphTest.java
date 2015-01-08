package peapod;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import peapod.model.Person;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FramedGraphTest {

    private FramedGraph graph = new FramedGraph(TinkerGraph.open());
    private Graph g;

    @Before
    public void init() {
        g = TinkerGraph.open();
        g.addVertex(T.id, 1, T.label, "person", "name", "willem");
        graph = new FramedGraph(g);
    }

    @Test
    public void testAddVertex() throws Exception {
        Person person = graph.addVertex(Person.class);
        assertNotNull(person);
        assertNotNull(person.vertex());
        assertNotNull(person.vertex().id());
        assertEquals("person", person.vertex().label());
    }

    @Test
    public void testAddVertexWithId() throws Exception {
        Person person = graph.addVertex(Person.class, 456);
        assertNotNull(person);
        assertNotNull(person.vertex());
        assertEquals(456, person.vertex().id());
        assertEquals("person", person.vertex().label());
    }

    @Test
    public void testV() throws Exception {
        Person p = graph.v(1, Person.class);
        assertEquals(1, p.vertex().id());
        assertEquals("willem", p.getName());
    }

    @Test
    public void testConfiguration() throws Exception {
        assertEquals(g.configuration(), graph.configuration());
    }

    @Test
    public void testVariables() throws Exception {
        assertEquals(g.variables(), graph.variables());
    }

    @Test
    @Ignore
    public void testFeatures() throws Exception {
        assertEquals(g.features(), graph.features());
    }

    @Test
    public void testGraph() throws Exception {
        assertEquals(g, graph.graph());
    }

    @Test
    public void testClose() throws Exception {
        graph.close();
    }
}