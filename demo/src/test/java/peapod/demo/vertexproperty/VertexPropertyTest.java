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

package peapod.demo.vertexproperty;

import com.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.junit.Test;
import peapod.FramedGraph;

import static org.hamcrest.collection.IsArrayContainingInAnyOrder.arrayContainingInAnyOrder;
import static org.junit.Assert.*;

public class VertexPropertyTest {

    @Test
    public void testVertexProperty() {
        FramedGraph g = new FramedGraph(TinkerGraph.open(), Person.class.getPackage());

        Person marko = g.addVertex(Person.class);
        marko.addName("marko");
        marko.addName("marko a. rodriguez");

        assertEquals(2, marko.getNames().size());
        assertThat(marko.getNames().stream().map(Name::getValue).toArray(), arrayContainingInAnyOrder("marko", "marko a. rodriguez"));

        Name name = marko.getName("marko");
        assertNotNull(name);
        name.setAcl("private");

        name = marko.getName("marko a. rodriguez");
        name.setAcl("public");

        name = marko.getNameWithAcl("public");
        assertEquals("marko a. rodriguez", name.getValue());
        assertEquals("public", name.getAcl());
        marko.removeName("marko a. rodriguez");
        assertNull(marko.getNameWithAcl("public"));

        name = marko.getNameWithAcl("private");
        assertEquals("private", name.getAcl());
        assertEquals("marko", name.getValue());

        name.setDate(2014);
        name.setCreator("stephen");

        name = marko.getNameWithAcl("private");
        assertEquals("stephen", name.getCreator());
        assertEquals(new Integer(2014), name.getDate());
    }

}
