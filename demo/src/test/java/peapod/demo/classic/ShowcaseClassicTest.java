/*
 * Copyright 2015-Bay of Many
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
 * This project is derived from code in the Tinkerpop project under the following licenses:
 *
 * Tinkerpop3
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the TinkerPop nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL TINKERPOP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package peapod.demo.classic;

import com.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import peapod.FramedGraph;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


public class ShowcaseClassicTest {

    private FramedGraph graph;
    private Person marko;
    private Person vadas;
    private Software lop;
    private Person josh;
    private Software ripple;
    private Person peter;

    @Before
    public void init() {
        TinkerGraph classic = TinkerFactory.createClassic();
        graph = new FramedGraph(classic);

        marko = graph.v(1, Person.class);
        vadas = graph.v(2, Person.class);
        lop = graph.v(3, Software.class);
        josh = graph.v(4, Person.class);
        ripple = graph.v(5, Software.class);
        peter = graph.v(6, Person.class);
    }

    @Test
    public void testProperties() {
        assertEquals("marko", marko.getName());
        assertEquals(new Integer(29), marko.getAge());

        assertEquals("vadas", vadas.getName());
        assertEquals(new Integer(27), vadas.getAge());

        assertEquals("lop", lop.getName());
        assertEquals("java", lop.getLang());

        assertEquals("josh", josh.getName());
        assertEquals(new Integer(32), josh.getAge());

        assertEquals("ripple", ripple.getName());
        assertEquals("java", ripple.getLang());

        assertEquals("peter", peter.getName());
        assertEquals(new Integer(35), peter.getAge());
    }

    @Test
    public void testEqualsAndHashcode() {
        assertEquals(graph.v(1, Person.class), graph.v(1, Person.class));
        assertEquals(graph.v(1, Person.class).hashCode(), graph.v(1, Person.class).hashCode());
    }

    @Test
    public void testNearVertices() {
        assertThat(marko.getKnows(), containsInAnyOrder(vadas, josh));

        assertThat(marko.getCreated(), containsInAnyOrder(lop));
        assertThat(peter.getCreated(), containsInAnyOrder(lop));
        assertThat(josh.getCreated(), containsInAnyOrder(lop, ripple));

    }

    @Test
    public void testNearEdges() {
        assertThat(marko.getCreatedEdge(), hasSize(1));
        Created created = marko.getCreatedEdge().get(0);
        assertEquals(new Float(0.4), created.getWeight());
        assertEquals(marko, created.getPerson());
        assertEquals(lop, created.getSoftware());

        assertThat(marko.getKnowsEdge(), hasSize(2));
        Optional<Knows> knowsVadas = marko.getKnowsEdge().stream().filter(k -> k.getOtherPerson().getName().equals("vadas")).findFirst();
        Optional<Knows> knowsJosh = marko.getKnowsEdge().stream().filter(k -> k.getOtherPerson().getName().equals("josh")).findFirst();

        assertTrue(knowsVadas.isPresent());
        assertEquals(marko, knowsVadas.get().getPerson());
        assertEquals(vadas, knowsVadas.get().getOtherPerson());
        assertEquals(new Float(0.5), knowsVadas.get().getWeight());

        assertTrue(knowsJosh.isPresent());
        assertEquals(marko, knowsJosh.get().getPerson());
        assertEquals(josh, knowsJosh.get().getOtherPerson());
        assertEquals(new Float(1.0), knowsJosh.get().getWeight());
    }

}
