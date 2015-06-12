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

package peapod.inheritance;

import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;
import peapod.GraphTest;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class InheritanceTest extends GraphTest {

    private FramedGraph graph;
    private Vertex vertex;

    @Before
    public void init() {
        g.addVertex(T.label, "Person", "name", "alice");
        vertex = g.addVertex(T.label, "Programmer", "name", "bob", "yearsExperience", 10);

        graph = new FramedGraph(g, Person.class.getPackage());
    }

    @Test
    public void testFind() {
        List<Person> persons = graph.V(Person.class).toList();
        assertEquals(2, persons.size());
        assertEquals(1, persons.stream().filter(p -> p instanceof Programmer).count());
    }

    @Test
    public void testLoad() {
        Person person = graph.v(vertex.id());
        assertTrue(person instanceof Programmer);
        assertEquals(10, ((Programmer)person).getYearsExperience());
    }
}
