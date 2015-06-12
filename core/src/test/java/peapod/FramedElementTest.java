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

package peapod;

import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import peapod.model.Person;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FramedElementTest extends GraphTest {

    private FramedGraph graph;

    private Person person;
    private Vertex v;

    @Before
    public void init() {
        v = g.addVertex(T.label, "Person", "name", "alice");
        graph = new FramedGraph(g, Person.class.getPackage());
        person = graph.v(v.id());
    }

    @Test
    public void testElement() throws Exception {
        assertEquals(v, person.element());
    }

    @Test
    public void testGraph() throws Exception {
        assertEquals(graph, person.graph());
    }

    @Test
    public void testId() throws Exception {
        assertEquals(v.id(), person.id());
    }

    @Test
    public void testRemove() throws Exception {
        person.remove();
        assertTrue(!g.traversal().V(Person.class).hasNext());
    }

    @Test
    public void testEquals() {
        assertEquals(graph.<Person>v(v.id()), graph.<Person>v(v.id()));
    }

    @Test
    public void testHashCode() {
        assertEquals(v.hashCode(), person.hashCode());
    }

}