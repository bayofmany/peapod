package peapod.demo.crew;

import com.tinkerpop.gremlin.structure.VertexProperty;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import peapod.FramedGraph;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertEquals;


/**
 * Created by Willem on 26/12/2014.
 */
public class ShowcaseCrewTest {

    @Test
    public void testGraph() {
        TinkerGraph g = TinkerFactory.createTheCrew();
        FramedGraph graph = new FramedGraph(g);

        Person marko = graph.v(1, Person.class);
        assertEquals("marko", marko.getName());
        assertEquals(true, marko.getVisible());

        Iterator<VertexProperty<String>> it = marko.vertex().iterators().propertyIterator("location");
        while (it.hasNext()) {
            VertexProperty<String> property = it.next();
            System.out.println("location = " + property);
            System.out.println("location = " + property.value());
            System.out.println("location = " + property.label());
            System.out.println("location = " + property.value("startTime"));
        }

     /*   Person vadas = graph.v(2, Person.class);
        assertEquals("vadas", vadas.getName());
        assertEquals(true, marko.getVisible());

        Software lop = graph.v(3, Software.class);
        assertEquals("lop", lop.getName());
        assertEquals("java", lop.getLang());

        Person josh = graph.v(4, Person.class);
        assertEquals("josh", josh.getName());
        assertEquals(true, marko.getVisible());

        Software ripple = graph.v(5, Software.class);
        assertEquals("ripple", ripple.getName());
        assertEquals("java", ripple.getLang());*/

        Person stephen = graph.v(7, Person.class);
        assertEquals("stephen", stephen.getName());
        assertEquals(true, stephen.getVisible());

        Person matthias = graph.v(8, Person.class);
        assertEquals("matthias", matthias.getName());
        assertEquals(true, matthias.getVisible());

        Person daniel = graph.v(9, Person.class);
        assertEquals("daniel", daniel.getName());
        assertEquals(false, daniel.getVisible());

    }
}
