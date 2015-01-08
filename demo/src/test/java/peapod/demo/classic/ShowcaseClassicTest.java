package peapod.demo.classic;

import com.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import peapod.FramedGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


/**
 * Created by Willem on 26/12/2014.
 */
public class ShowcaseClassicTest {

    private FramedGraph graph;
    private Person marko;
    private Person vadas;
    private Software lop;
    private Person josh;
    private Software ripple;
    private Person peter;

    @Before
    public void init() {
        TinkerGraph classic = TinkerFactory.createClassic();
        graph = new FramedGraph(classic);

        marko = graph.v(1, Person.class);
        vadas = graph.v(2, Person.class);
        lop = graph.v(3, Software.class);
        josh = graph.v(4, Person.class);
        ripple = graph.v(5, Software.class);
        peter = graph.v(6, Person.class);
    }

    @Test
    public void testProperties() {
        assertEquals("marko", marko.getName());
        assertEquals(new Integer(29), marko.getAge());

        assertEquals("vadas", vadas.getName());
        assertEquals(new Integer(27), vadas.getAge());

        assertEquals("lop", lop.getName());
        assertEquals("java", lop.getLang());

        assertEquals("josh", josh.getName());
        assertEquals(new Integer(32), josh.getAge());

        assertEquals("ripple", ripple.getName());
        assertEquals("java", ripple.getLang());

        assertEquals("peter", peter.getName());
        assertEquals(new Integer(35), peter.getAge());
    }

    @Test
    public void testEqualsAndHashcode() {
        assertEquals(graph.v(1, Person.class), graph.v(1, Person.class));
        assertEquals(graph.v(1, Person.class).hashCode(), graph.v(1, Person.class).hashCode());
    }

    @Test
    public void testNearVertices() {
        assertThat(marko.getKnows(), containsInAnyOrder(vadas, josh));

        assertThat(marko.getCreated(), containsInAnyOrder(lop));
        assertThat(peter.getCreated(), containsInAnyOrder(lop));
        assertThat(josh.getCreated(), containsInAnyOrder(lop, ripple));

    }

    @Test
    public void testNearEdges() {
        assertThat(marko.getCreatedEdge(), hasSize(1));
        Created created = marko.getCreatedEdge().get(0);
        assertEquals(new Float(0.4), created.getWeight());
        assertEquals(marko, created.getPerson());
        assertEquals(lop, created.getSoftware());

        assertThat(marko.getKnowsEdge(), hasSize(2));
        Optional<Knows> knowsVadas = marko.getKnowsEdge().stream().filter(k -> k.getOtherPerson().getName().equals("vadas")).findFirst();
        Optional<Knows> knowsJosh = marko.getKnowsEdge().stream().filter(k -> k.getOtherPerson().getName().equals("josh")).findFirst();

        assertTrue(knowsVadas.isPresent());
        assertEquals(marko, knowsVadas.get().getPerson());
        assertEquals(vadas, knowsVadas.get().getOtherPerson());
        assertEquals(new Float(0.5), knowsVadas.get().getWeight());

        assertTrue(knowsJosh.isPresent());
        assertEquals(marko, knowsJosh.get().getPerson());
        assertEquals(josh, knowsJosh.get().getOtherPerson());
        assertEquals(new Float(1.0), knowsJosh.get().getWeight());
    }

}
