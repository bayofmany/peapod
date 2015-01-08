package peapod.linkedvertex;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Created by wisa on 07/01/2015.
 */
public class LinkedVertexTest {

    private Person alice;
    private Vertex v;
    private FramedGraph graph;

    @Before
    public void init() {
        Graph g = TinkerGraph.open();
        Vertex alice = g.addVertex(T.id, 1, T.label, "person", "name", "alice");
        Vertex bob = g.addVertex(T.id, 2, T.label, "person", "name", "bob");
        Vertex steve = g.addVertex(T.id, 3, T.label, "person", "name", "steve");
        alice.addEdge("friend", bob);
        alice.addEdge("friend", steve);

        graph = new FramedGraph(g);
        this.alice = graph.v(1, Person.class);
    }

    @Test
    public void testOutVertices() {
        assertEquals(2, alice.getFriends().size());
        Person bob = graph.v(2, Person.class);
        assertThat(alice.getFriends(), hasItems(bob, graph.v(3, Person.class)));
        assertEquals(0, bob.getFriends().size());
    }

    @Test
    public void testLinkedVerticesWithDefaultAnnotation() {
        assertEquals(2, alice.getFriendsWithAnnotationDefault().size());
        assertThat(alice.getFriends(), hasItems(graph.v(2, Person.class), graph.v(3, Person.class)));
    }

    @Test
    public void testLinkedVerticesWithDefaultAnnotationIn() {
        assertEquals(0, alice.getFriendsWithAnnotationIn().size());

        Person bob = graph.v(2, Person.class);
        assertEquals(1, bob.getFriendsWithAnnotationIn().size());
        assertThat(bob.getFriendsWithAnnotationIn(), hasItems(graph.v(1, Person.class)));
    }

    @Test
    public void testLinkedVerticesWithDefaultAnnotationBoth() {
        Person bob = graph.v(2, Person.class);
        Person steve = graph.v(3, Person.class);

        assertThat(alice.getFriendsWithAnnotationBoth(), hasItems(bob, steve));
        assertThat(bob.getFriendsWithAnnotationBoth(), hasItems(alice));
        assertThat(steve.getFriendsWithAnnotationBoth(), hasItems(alice));
    }
}
