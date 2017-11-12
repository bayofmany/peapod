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

import org.apache.tinkerpop.gremlin.process.traversal.P;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Graph.Features;
import org.apache.tinkerpop.gremlin.structure.Graph.Variables;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.configuration.Configuration;
import org.reflections.Reflections;
import peapod.internal.runtime.Framer;
import peapod.internal.runtime.FramerRegistry;
import peapod.internal.runtime.IFramer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * <p>A framed instance of a TinkerPop 3 graph.</p>
 * <p>Allows to query the graph and return framed objects instead of TinkerPop 3 {@code vertices} and {@code edges}</p>
 * <p>The provided package is used to recursively scan all {@code @Vertex}, {@code @VertexProperties} and {@code @Edge} classes.</p>
 * <pre>
 *     FramedGraph graph = new FramedGraph(TinkerGraph.open(), Person.class.getPackage());
 *
 *     Person person = graph.addVertex(1, Person.class);
 *     person.setName("alice");
 *
 *     List&lt;Person&gt; result = graph.V(Person.class).has("name", "alice").toList();
 *     assertEquals(1, result.size());
 * </pre>
 *
 * @author Willem Salembier
 * @see org.apache.tinkerpop.gremlin.structure.Graph
 * @since 0.1
 */
public class FramedGraph implements AutoCloseable {

    private final Graph graph;

    private final GraphTraversalSource traversal;

    private FramerRegistry registry = new FramerRegistry();


    public FramedGraph(Graph graph, Package pakkage) {
        this.graph = graph;
        this.traversal = graph.traversal();
        this.registry.register(new Reflections(pakkage.getName() + ".").getTypesAnnotatedWith(Framer.class));
    }

    /**
     * Add a linked vertex of type {@link V} to the graph. The value will be the lowercase value of the class.
     *
     * @param <V>   Framing class annotated with  {@link peapod.annotations.Vertex}
     * @param clazz a framing class annotated with {@link peapod.annotations.Vertex}
     * @return The newly created labeled linked vertex
     */
    public <V> V addVertex(Class<V> clazz) {
        IFramer<Element, V> framer = registry.get(clazz);
        Vertex v = graph.addVertex(framer.label());
        return framer.frameNew(v, this);
    }

    /**
     * Add a linked vertex of type {@link V} and given id to the graph. The value will be the lowercase value of the class.
     *
     * @param <V>   Framing class annotated with  {@link peapod.annotations.Vertex}
     * @param clazz a framing class annotated with {@link peapod.annotations.Vertex}
     * @param id    the user-supplied identifier of the vertex. Attention, not all databases support this feature.
     * @return The newly created labeled linked vertex
     * @see org.apache.tinkerpop.gremlin.structure.Graph.Features.ElementFeatures#supportsUserSuppliedIds()
     */
    public <V> V addVertex(Class<V> clazz, Object id) {
        IFramer<Element, V> framer = registry.get(clazz);
        Vertex v = graph.addVertex(T.id, id, T.label, framer.label());
        return framer.frameNew(v, this);
    }

    @SuppressWarnings("unchecked")
    public <V> FramedGraphTraversal<V> V(Class<V> clazz) {
        return new FramedGraphTraversal(traversal.V(), this).labels(clazz, P.within(registry.labels(clazz)));
    }

    /**
     * Get a {@link Vertex} given its unique identifier.
     *
     * @param <V> Framing class annotated with  {@link peapod.annotations.Vertex}
     * @param id  The unique identifier of the linked vertex to locate
     * @return the framed vertex or {@code null} when not found
     */
    @SuppressWarnings("unchecked")
    public <V> V v(Object id) {
        Iterator<Vertex> it = graph.vertices(id);
        return it.hasNext() ? frame(it.next()) : null;
    }

    /**
     * Get a {@link Vertex} given its unique identifier.
     *
     * @param <V> Framing class annotated with  {@link peapod.annotations.Vertex}
     * @param id  The unique identifier of the linked vertex to locate
     * @param clazz a framing class annotated with {@link peapod.annotations.Vertex}
     * @return the framed vertex or {@code null} when not found
     */
    @SuppressWarnings("unchecked")
    public <V> V v(Object id, Class<V> clazz) {
        Iterator<Vertex> tr = graph.vertices(id);
        return tr.hasNext() ? frame(tr.next(), clazz) : null;
    }

    public <F, E extends Element> F frame(E e) {
        IFramer<E, F> framer = registry.get(e, null);
        return framer.frame(e, this);
    }

    public <F, E extends Element> F frame(E e, Class<F> clazz) {
        IFramer<E, F> framer = registry.get(e, clazz);
        return framer.frame(e, this);
    }

    public <F, E extends Element> List<F> frame(Iterator<E> it, Class<F> clazz) {
        List<F> result = new ArrayList<>();
        it.forEachRemaining(e -> {
            IFramer<E, F> framer = registry.get(e, clazz);
            result.add(framer.frame(e, this));
        });
        return Collections.unmodifiableList(result);
    }

    protected <F, E extends Element> IFramer<E, F> framer(Class<F> clazz) {
        return registry.get(clazz);
    }

    /**
     * Configure and control the transactions for those graphs that support this feature.
     *
     * @return the transaction
     * @see org.apache.tinkerpop.gremlin.structure.Graph#tx()
     */
    public Transaction tx() {
        return graph.tx();
    }

    /**
     * A collection of global {@link Variables} associated with the graph.
     * Variables are used for storing metadata about the graph.
     *
     * @return The variables associated with this graph
     * @see org.apache.tinkerpop.gremlin.structure.Graph#variables()
     */
    public Variables variables() {
        return graph.variables();
    }

    /**
     * Get the {@link org.apache.commons.configuration.Configuration} associated with the construction of this graph.
     * Whatever configuration was passed to {@link org.apache.tinkerpop.gremlin.structure.util.GraphFactory#open(org.apache.commons.configuration.Configuration)}
     * is what should be returned by this method.
     *
     * @return the configuration used during graph construction.
     * @see org.apache.tinkerpop.gremlin.structure.Graph#configuration()
     */
    public Configuration configuration() {
        return graph.configuration();
    }

    /**
     * Gets the {@link Features} exposed by the underlying {@code Graph} implementation.
     *
     * @return a features object
     * @see org.apache.tinkerpop.gremlin.structure.Graph#features()
     */
    public Features features() {
        return graph.features();
    }

    /**
     * @return the underlying {@link org.apache.tinkerpop.gremlin.structure.Graph}
     */
    public Graph graph() {
        return graph;
    }

    protected GraphTraversalSource traversal() {
        return traversal;
    }

    @Override
    public void close() throws Exception {
        graph.close();
    }
}
