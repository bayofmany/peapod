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

package peapod.vertexproperty;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Vertex;
import com.tinkerpop.gremlin.structure.VertexProperty;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;
import peapod.FramedVertex;

import java.util.Optional;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.*;

public class VertexPropertyTest {

    private Person alice;
    private Vertex v;

    @Before
    public void init() {
        Graph g = TinkerGraph.open();
        Vertex v = g.addVertex(T.id, 1, T.label, "Person", "name", "Alice");
        v.property("location", "Brussels", "startTime", 2010, "endTime", 2012);
        v.property("location", "Antwerp", "startTime", 2012);


        FramedGraph graph = new FramedGraph(g);
        alice = graph.v(1, Person.class);
        this.v = ((FramedVertex) alice).vertex();
    }

    @Test
    public void testGetLocations() {
        assertThat(alice.getLocations().stream().map(Location::getValue).toArray(), arrayContainingInAnyOrder("Brussels", "Antwerp"));
    }

    @Test
    public void testGetLocation() {
        Location brussels = alice.getLocation("Brussels");
        assertEquals("Brussels", brussels.getValue());
        assertEquals(new Integer(2010), brussels.getStartTime());
        assertEquals(new Integer(2012), brussels.getEndTime());

        Location antwerp = alice.getLocation("Antwerp");
        assertEquals("Antwerp", antwerp.getValue());
        assertEquals(new Integer(2012), antwerp.getStartTime());
        assertNull(antwerp.getEndTime());
    }

    @Test
    public void testAddLocation() {
        Location location = alice.addLocation("London");
        assertEquals("London", location.getValue());

        location.setStartTime(2015);

        Optional<VertexProperty<Integer>> vp = v.properties("location").<VertexProperty<Integer>>has(T.value, "London").tryNext();
        assertTrue(vp.isPresent());
        assertEquals(new Integer(2015), vp.get().<Integer>value("startTime"));
    }

    @Test
    public void testRemoveLocation() {
        alice.removeLocation("Brussels");
        assertFalse(v.properties("location").has(T.value, "Brussels").hasNext());
    }
}
