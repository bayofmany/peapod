package peapod.edge;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Created by wisa on 07/01/2015.
 */
public class EdgeTest {

    private Person alice;
    private Person bob;
    private Person steve;
    private Vertex v;
    private FramedGraph graph;

    @Before
    public void init() {
        Graph g = TinkerGraph.open();
        Vertex alice = g.addVertex(T.id, 1, T.label, "person", "name", "alice");
        Vertex bob = g.addVertex(T.id, 2, T.label, "person", "name", "bob");
        Vertex steve = g.addVertex(T.id, 3, T.label, "person", "name", "steve");
        alice.addEdge("friend", bob, "startYear", 2004);
        alice.addEdge("friend", steve, "startYear", 2012);

        graph = new FramedGraph(g);
        this.alice = graph.v(1, Person.class);
        this.bob = graph.v(2, Person.class);
        this.steve = graph.v(3, Person.class);
    }

    @Test
    public void testLinkedEdgeDefault() {
        assertEquals(2, alice.getFriends().size());

        Optional<Friend> friendBob = alice.getFriends().stream().filter(f -> f.getFriend().equals(bob)).findFirst();
        assertTrue(friendBob.isPresent());
        assertEquals(2004, friendBob.get().getStartYear());

        assertEquals(bob, friendBob.get().getFriend());
        assertEquals(0, bob.getFriends().size());
    }

    @Test
    public void testLinkedEdgeAnnotationOut() {
        List<Person> friends = alice.getFriendsWithAnnotationOut().stream().map(f -> f.getFriend()).collect(Collectors.toList());
        assertThat(friends, containsInAnyOrder(bob, steve));

        friends = bob.getFriendsWithAnnotationOut().stream().map(f -> f.getFriend()).collect(Collectors.toList());
        assertThat(friends, empty());
    }

    @Test
    public void testLinkedEdgeAnnotationIn() {
        List<Person> friends = bob.getFriendsWithAnnotationIn().stream().map(f -> f.getPerson()).collect(Collectors.toList());
        assertThat(friends, containsInAnyOrder(alice));

        friends = alice.getFriendsWithAnnotationIn().stream().map(f -> f.getPerson()).collect(Collectors.toList());
        assertThat(friends, empty());
    }

    @Test
    public void testLinkedEdgeAnnotationBoth() {
        List<Person> friends = bob.getFriendsWithAnnotationBoth().stream().map(f -> f.getPerson()).collect(Collectors.toList());
        assertThat(friends, containsInAnyOrder(alice));

        friends = alice.getFriendsWithAnnotationBoth().stream().map(f -> f.getFriend()).collect(Collectors.toList());
        assertThat(friends, containsInAnyOrder(bob, steve));
    }

}
