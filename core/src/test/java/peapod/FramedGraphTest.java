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

package peapod;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import peapod.model.Person;

import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class FramedGraphTest extends GraphTest {

    private FramedGraph graph;
    private Vertex alice;

    @Before
    public void init() {
        alice = g.addVertex(T.label, "Person", "name", "alice");
        Vertex b = g.addVertex(T.label, "Person", "name", "bob");
        Vertex c = g.addVertex(T.label, "Person", "name", "charlie");
        alice.addEdge("friend", b);
        alice.addEdge("friend", c);
        graph = new FramedGraph(g);
    }

    @Test
    public void testAddVertex() throws Exception {
        Person person = graph.addVertex(Person.class);
        assertNotNull(person);
        assertNotNull(person.vertex());
        assertNotNull(person.vertex().id());
        assertEquals("Person", person.vertex().label());
    }

    @Test
    public void testAddVertexWithId() throws Exception {
        assumeTrue(graph.features().vertex().supportsUserSuppliedIds());

        Person person = graph.addVertex(Person.class, 456);
        assertNotNull(person);
        assertNotNull(person.vertex());
        assertEquals(456, person.vertex().id());
        assertEquals("Person", person.vertex().label());
    }

    @Test
    public void testV() throws Exception {
        Person p = graph.v(alice.id(), Person.class);
        assertEquals(alice.id(), p.vertex().id());
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
    public void testIn() throws Exception {
        List<Person> result = graph.V(Person.class).has("name", "bob").in("friend", Person.class).toList();
        assertEquals(1, result.size());
        assertEquals("alice", result.get(0).getName());

        assertThat(graph.v(alice.id(), Person.class).getFriends(), hasItem(hasProperty("name", equalTo("bob"))));
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
    public void testTx() throws Exception {
        assumeTrue(graph.features().graph().supportsTransactions());
        assertNotNull(graph.tx());
    }

}