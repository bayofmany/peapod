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

package peapod.linkededge;

import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;
import peapod.GraphTest;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.junit.Assert.*;

public class LinkedEdgeTest extends GraphTest {

    private Person alice;
    private Person bob;
    private Person charlie;

    @Before
    public void init() {
        Vertex alice = g.addVertex(T.label, "Person", "value", "alice");
        Vertex bob = g.addVertex(T.label, "Person", "value", "bob");
        Vertex charlie = g.addVertex(T.label, "Person", "value", "charlie");
        alice.addEdge("friend", bob, "startYear", 2004);
        alice.addEdge("friend", charlie, "startYear", 2012);

        FramedGraph graph = new FramedGraph(g, Person.class.getPackage());
        this.alice = graph.v(alice.id());
        this.bob = graph.v(bob.id());
        this.charlie = graph.v(charlie.id());
    }

    @Test
    public void testLinkedEdgeDefault() {
        assertEquals(2, alice.getFriendsWithAnnotationDefault().size());

        Optional<Friend> friendBob = alice.getFriendsWithAnnotationDefault().stream().filter(f -> f.getFriend().equals(bob)).findFirst();
        assertTrue(friendBob.isPresent());
        assertEquals(2004, friendBob.get().getStartYear());

        assertEquals(bob, friendBob.get().getFriend());
        assertEquals(0, bob.getFriendsWithAnnotationDefault().size());
    }

    @Test
    public void testLinkedEdgeAnnotationOut() {
        List<Person> friends = alice.getFriendsWithAnnotationOut().stream().map(Friend::getFriend).collect(Collectors.toList());
        assertThat(friends, containsInAnyOrder(bob, charlie));

        friends = bob.getFriendsWithAnnotationOut().stream().map(Friend::getFriend).collect(Collectors.toList());
        assertThat(friends, empty());
    }

    @Test
    public void testLinkedEdgeAnnotationIn() {
        List<Person> friends = bob.getFriendsWithAnnotationIn().stream().map(Friend::getPerson).collect(Collectors.toList());
        assertThat(friends, containsInAnyOrder(alice));

        friends = alice.getFriendsWithAnnotationIn().stream().map(Friend::getPerson).collect(Collectors.toList());
        assertThat(friends, empty());
    }

    @Test
    public void testLinkedEdgeAnnotationBoth() {
        List<Person> friends = bob.getFriendsWithAnnotationBoth().stream().map(Friend::getPerson).collect(Collectors.toList());
        assertThat(friends, containsInAnyOrder(alice));

        friends = alice.getFriendsWithAnnotationBoth().stream().map(Friend::getFriend).collect(Collectors.toList());
        assertThat(friends, containsInAnyOrder(bob, charlie));
    }

}
