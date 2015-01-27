/*
 * Copyright 2015 Bay of Many
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 * This project is derived from code in the Tinkerpop project under the following license:
 *
 *    Tinkerpop3
 *    http://www.apache.org/licenses/LICENSE-2.0
 */

package peapod.manytomany;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;
import peapod.GraphProvider;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;

public class ManyToManyTest {

    private Person alice;
    private Person bob;
    private Person charlie;

    @Before
    public void init() {
        Graph g = GraphProvider.getGraph();
        Vertex alice = g.addVertex(T.label, "Person", "name", "alice");
        Vertex bob = g.addVertex(T.label, "Person", "name", "bob");
        Vertex charlie = g.addVertex(T.label, "Person", "name", "charlie");

        alice.addEdge("friend", bob);

        FramedGraph graph = new FramedGraph(g);
        this.alice = graph.v(alice.id(), Person.class);
        this.bob = graph.v(bob.id(), Person.class);
        this.charlie = graph.v(charlie.id(), Person.class);
    }

    @Test
    public void testGetList() {
        assertEquals(1, alice.getFriends().size());
    }

    @Test
    public void testAdd() {
        alice.addFriend(charlie);
        List<Vertex> f = alice.vertex().out("friend").toList();
        assertThat(f, containsInAnyOrder(bob.vertex(), charlie.vertex()));
    }

    @Test
    public void testRemove() {
        alice.removeFriend(bob);
        assertTrue(alice.out("friend").toList().isEmpty());
    }
}
