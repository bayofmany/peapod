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
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import peapod.model.Person;

import static org.junit.Assert.*;

public class FramedElementTest {

    private FramedGraph graph = new FramedGraph(TinkerGraph.open());
    private Graph g;
    private Person person;

    @Before
    public void init() {
        g = TinkerGraph.open();
        g.addVertex(T.id, 1, T.label, "Person", "name", "alice");
        graph = new FramedGraph(g);
        person = graph.v(1, Person.class);
    }

    @Test
    public void testElement() throws Exception {
        assertEquals(g.V(1).next(), person.element());
    }

    @Test
    public void testGraph() throws Exception {
        assertEquals(graph, person.graph());
    }

    @Test
    public void testId() throws Exception {
        assertEquals(1, person.id());
    }

    @Test
    public void testRemove() throws Exception {
        person.remove();
        assertTrue(!g.V().hasNext());
    }
}