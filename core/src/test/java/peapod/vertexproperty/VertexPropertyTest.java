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

package peapod.vertexproperty;

import com.google.common.collect.Lists;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import org.junit.Before;
import org.junit.Test;
import peapod.FramedGraph;
import peapod.GraphTest;

import java.util.Iterator;

import static org.hamcrest.Matchers.arrayContainingInAnyOrder;
import static org.junit.Assert.*;
import static org.junit.Assume.assumeTrue;

public class VertexPropertyTest extends GraphTest {

    private Person alice;
    private Vertex v;

    @Before
    public void init() {
        assumeTrue(g.features().vertex().supportsMetaProperties());

        v = g.addVertex(T.label, "Person", "name", "Alice");
        v.property(VertexProperty.Cardinality.list, "location", "Brussels", "startTime", 2010, "endTime", 2012);
        v.property(VertexProperty.Cardinality.list, "location", "Antwerp", "startTime", 2012);

        FramedGraph graph = new FramedGraph(g, Person.class.getPackage());
        this.alice = graph.v(v.id());
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

        Iterator<VertexProperty<String>> it = v.properties("location");
        VertexProperty london = null;
        while (it.hasNext()) {
            VertexProperty property = it.next();
            if (property.value().equals("London")) {
                london = property;
            }
        }
        assertNotNull("London", london);
        assertEquals(new Integer(2015), london.value("startTime"));
    }

    @Test
    public void testRemoveLocation() {
        alice.removeLocation("Brussels");
        assertFalse(Lists.newArrayList(v.values("location")).contains("Brussels"));
    }
}
