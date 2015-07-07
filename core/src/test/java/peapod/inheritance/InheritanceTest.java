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

        Vertex v1 = g.addVertex(T.label, "Dog", "name", "Shepherd", "numberOfLegs", 4, "hairColor", "brown");
        g.addVertex(T.label, "Mammal", "name", "Tiger", "numberOfLegs", 4);
        g.addVertex(T.label, "Salmon", "name", "Atlantic Salmon", "numberOfLegs", 0, "saltWater", true);
        g.addVertex(T.label, "Fish", "name", "Goldfish", "numberOfLegs", 0, "saltWater", false);
        Vertex v2 = g.addVertex(T.label, "GOM", "name", "WeirdAnimal", "numberOfLegs", 2, "saltWater", true);

        v2.addEdge("relatedto", v1, "relation", "same color");

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
        assertEquals(10, ((Programmer) person).getYearsExperience());
    }

    @Test
    public void testFindWithInterfaces() {
        assertEquals(5, graph.V(Animal.class).toList().size());
        assertEquals(3, graph.V(Mammal.class).toList().size());
        assertEquals(3, graph.V(Fish.class).toList().size());
        assertEquals(2, graph.V(Salmon.class).toList().size());
        assertEquals(2, graph.V(Dog.class).toList().size());

        Dog dog = graph.V(Dog.class).has("name", "Shepherd").next();
        assertEquals(4, dog.getNumberOfLegs());
        assertEquals("brown", dog.getHairColor());

        Salmon salmon = graph.V(Salmon.class).has("name", "Atlantic Salmon").next();
        assertEquals(0, salmon.getNumberOfLegs());
        assertTrue(salmon.getSaltWater());

        Fish fish = graph.V(Fish.class).has("saltWater", false).next();
        assertEquals("Goldfish", fish.getName());

        Animal gom = graph.V(Animal.class).has("name", "WeirdAnimal").next();
        assertTrue(gom instanceof GeneticallyModifiedOrganism);

        assertEquals(1, gom.getRelatedTo().size());
        RelatedTo relatedTo = gom.getRelatedTo().get(0);
        assertEquals(gom, relatedTo.getMe());
        assertEquals(dog, relatedTo.getOther());
        assertEquals("same color", relatedTo.getRelation());
    }

}
