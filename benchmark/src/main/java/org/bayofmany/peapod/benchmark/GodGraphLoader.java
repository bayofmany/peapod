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
package org.bayofmany.peapod.benchmark;

import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;

/**
 * Example Graph factory that creates a graph based on roman mythology.
 */
public class GodGraphLoader {

    public static void load(final TinkerGraph graph) {

        // vertices
        graph.createIndex("name", Vertex.class);

        Vertex saturn = graph.addVertex(T.label, "god", "name", "saturn", "age", 10000, "type", "titan", "implementation_type", FermaGod.class.getName());

        Vertex sky = graph.addVertex(T.label, "god", "name", "sky", "type", "location", "other", "more useless info");

        Vertex sea = graph.addVertex(T.label, "god", "name", "sea", "type", "location");

        Vertex jupiter = graph.addVertex(T.label, "god", "name", "jupiter", "age", 5000, "type", "god", "implementation_type", FermaGod.class.getName());

        Vertex neptune = graph.addVertex(T.label, "god", "name", "neptune", "age", 4500, "type", "god", "implementation_type", FermaGod.class.getName());

        Vertex hercules = graph.addVertex(T.label, "god", "name", "hercules", "age", 30, "type", "demigod", "implementation_type", FermaGod.class.getName());

        Vertex alcmene = graph.addVertex(T.label, "god", "name", "alcmene", "age", 45, "type", "human", "implementation_type", FermaGod.class.getName());

        Vertex pluto = graph.addVertex(T.label, "god", "name", "pluto", "age", 4000, "type", "god", "implementation_type", FermaGod.class.getName());

        Vertex nemean = graph.addVertex(T.label, "god", "name", "nemean", "type", "monster", "implementation_type", FermaGod.class.getName());

        Vertex hydra = graph.addVertex(T.label, "god", "name", "hydra", "type", "monster", "implementation_type", FermaGod.class.getName());

        Vertex cerberus = graph.addVertex(T.label, "god", "name", "cerberus", "type", "monster", "implementation_type", FermaGod.class.getName());

        Vertex tartarus = graph.addVertex(T.label, "god", "name", "tartarus", "type", "location", "implementation_type", FermaGod.class.getName());

        Vertex nogod = graph.addVertex(T.label, "nogod", "name", "saturn", "type", "blabla", "implementation_type", FermaGod.class.getName());

        // edges

        jupiter.addEdge("father", saturn, "implementation_type", FatherEdge.class.getName());
        jupiter.addEdge("lives", sky, "reason", "loves fresh breezes");
        jupiter.addEdge("brother", neptune);
        jupiter.addEdge("brother", pluto);

        neptune.addEdge("father", saturn, "implementation_type", FatherEdge.class.getName());
        neptune.addEdge("lives", sea, "reason", "loves waves");
        neptune.addEdge("brother", jupiter);
        neptune.addEdge("brother", pluto);

        hercules.addEdge("father", jupiter, "implementation_type", FatherEdge.class.getName());
        hercules.addEdge("lives", sky, "reason", "loves heights");
        hercules.addEdge("battled", nemean, "time", 1);
        hercules.addEdge("battled", hydra, "time", 2);
        hercules.addEdge("battled", cerberus, "time", 12);

        pluto.addEdge("father", saturn, "implementation_type", FatherEdge.class.getName());
        pluto.addEdge("brother", jupiter);
        pluto.addEdge("brother", neptune);
        pluto.addEdge("lives", tartarus, "reason", "no fear of death");
        pluto.addEdge("pet", cerberus);

        cerberus.addEdge("lives", tartarus);
        cerberus.addEdge("battled", alcmene, "time", 5);

        // commit the transaction to disk
        if (graph.features().graph().supportsTransactions())
            graph.tx().commit();
    }
}