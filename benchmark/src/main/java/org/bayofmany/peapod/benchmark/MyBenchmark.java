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

import com.syncleus.ferma.DelegatingFramedGraph;
import com.syncleus.ferma.FramedGraph;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.util.Iterator;

@State(Scope.Benchmark)
public class MyBenchmark {

    private TinkerGraph godGraph;

    private peapod.FramedGraph peapodGraph;
    private FramedGraph fermaGraph;

    @Setup
    public void init() {
        godGraph = TinkerGraph.open();
        GodGraphLoader.load(godGraph);

        peapodGraph = new peapod.FramedGraph(godGraph, PeapodGod.class.getPackage());
        fermaGraph = new DelegatingFramedGraph<Graph>(godGraph, true, false);
    }

    @Benchmark
    public void testGetFramedVerticesTypedTinkerpop(Blackhole bh) {
        Vertex next = godGraph.traversal().V().hasLabel("god").has("name", "saturn").next();
        bh.consume(next.property("name").value());
        Iterator<Vertex> children = next.vertices(Direction.IN, "father");
        while (children.hasNext()) {
            Vertex child = children.next();
            Vertex father = child.vertices(Direction.OUT, "father").next();
            bh.consume(father);
        }
    }

    @Benchmark
    public void testGetFramedVerticesTypedPeapod(Blackhole bh) {
        PeapodGod god = peapodGraph.V(PeapodGod.class).has("name", "saturn").next();
        bh.consume(god.getName());
        for (PeapodGod child : god.getSons()) {
            PeapodGod father = child.getParents().iterator().next();
            bh.consume(father);
        }
    }

    @Benchmark
    public void testGetFramedVerticesTypedFerma(Blackhole bh) {
        FermaGod god = fermaGraph.traverse(s -> s.V().hasLabel("god").has("name", "saturn")).frame(FermaGod.class).next();
        bh.consume(god.getName());
        for (FermaGod child : god.getSons()) {
            FermaGod father = child.getParents().iterator().next();
            bh.consume(father);
        }
    }

}
