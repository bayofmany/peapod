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
 * This project is derived from code in the TinkerPop project under the following license:
 *
 *    TinkerPop3
 *    http://www.apache.org/licenses/LICENSE-2.0
 */

package peapod.manytomanyedge;

import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;
import peapod.GraphTest;

import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.*;
import static peapod.TinkerPopHelper.out;

public class ManyToManyEdgeTest extends GraphTest {

    private Person alice;
    private Person bob;
    private Person charlie;

    @Before
    public void init() {
        Vertex alice = g.addVertex(T.label, "Person", "name", "alice");
        Vertex bob = g.addVertex(T.label, "Person", "name", "bob");
        Vertex charlie = g.addVertex(T.label, "Person", "name", "charlie");

        alice.addEdge("friend", bob);

        FramedGraph graph = new FramedGraph(g, Person.class.getPackage());
        this.alice = graph.v(alice.id());
        this.bob = graph.v(bob.id());
        this.charlie = graph.v(charlie.id());
    }

    @Test
    public void testGetList() {
        assertEquals(1, alice.getFriends().size());
    }

    @Test
    public void testGetFiltered() {
        Friend friend = alice.getFriend(bob);
        assertNotNull(friend);
        assertEquals(bob, friend.getFriend());
    }


    @Test
    public void testAdd() {
        Friend friend = alice.addFriend(charlie);
        assertNotNull(friend);

        assertEquals(alice, friend.getPerson());
        assertEquals(charlie, friend.getFriend());

        List<Vertex> f = out(alice.vertex(), "friend");
        assertThat(f, containsInAnyOrder(bob.vertex(), charlie.vertex()));
    }

    @Test
    public void testRemove() {
        Friend friend = alice.getFriends().get(0);
        alice.removeFriend(friend);
        assertTrue(alice.out("friend").toList().isEmpty());
    }
}
