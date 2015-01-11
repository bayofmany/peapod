package peapod;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import peapod.model.Person;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class FramedGraphTest {

    private FramedGraph graph = new FramedGraph(TinkerGraph.open());
    private Graph g;

    @Before
    public void init() {
        g = TinkerGraph.open();
        g.addVertex(T.id, 1, T.label, "person", "name", "alice");
        g.addVertex(T.id, 2, T.label, "person", "name", " bob");
        g.addVertex(T.id, 3, T.label, "person", "name", "steve");
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
    public void testLoad() throws Exception {
        Person p = graph.v(1, Person.class);
        assertEquals(1, p.vertex().id());
        assertEquals("alice", p.getName());
    }

    @Test
    public void testFindAll() throws Exception {
        List<Person> result = graph.V(Person.class).toList();
        assertEquals(3, result.size());
        assertEquals("alice", result.get(0).getName());
    }

    @Test
    public void testHas() throws Exception {
        List<Person> result = graph.V(Person.class).has("name", "alice").toList();
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getName());
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
    public void testFeatures() throws Exception {
        assertEquals(g.features().toString(), graph.features().toString());
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