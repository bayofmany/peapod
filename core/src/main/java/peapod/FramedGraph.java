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

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.structure.Element;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Graph.Features;
import com.tinkerpop.gremlin.structure.Graph.Variables;
import com.tinkerpop.gremlin.structure.Transaction;
import com.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.configuration.Configuration;
import org.reflections.Reflections;
import peapod.internal.runtime.Framer;
import peapod.internal.runtime.FramerRegistry;
import peapod.internal.runtime.IFramer;

import java.util.NoSuchElementException;

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
 * @see com.tinkerpop.gremlin.structure.Graph
 * @since 0.1
 */
public class FramedGraph implements AutoCloseable {

    private final Graph graph;

    private FramerRegistry registry = new FramerRegistry();


    public FramedGraph(Graph graph, Package pakkage) {
        this.graph = graph;
        this.registry.register(new Reflections(pakkage.getName() + ".").getTypesAnnotatedWith(Framer.class));
    }

    /**
     * Add a linked vertex of type {@link V} to the graph. The value will be the lowercase value of the class.
     *
     * @param clazz a class implementing {@link FramedVertex} and annotated with {@link com.tinkerpop.gremlin.structure.Vertex}
     * @param <V> Framing class annotated with  {@link peapod.annotations.Vertex}
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
     * @param clazz a class implementing {@link FramedVertex} and annotated with {@link com.tinkerpop.gremlin.structure.Vertex}
     * @param <V> Framing class annotated with  {@link peapod.annotations.Vertex}
     * @return The newly created labeled linked vertex
     */
    public <V> V addVertex(Class<V> clazz, Object id) {
        IFramer<Element, V> framer = registry.get(clazz);
        Vertex v = graph.addVertex(T.id, id, T.label, framer.label());
        return framer.frameNew(v, this);
    }

    @SuppressWarnings("unchecked")
    public <S, V> FramedGraphTraversal<S, V> V(Class<V> clazz) {
        return new FramedGraphTraversal(graph().V(), this).labels(clazz, registry.labels(clazz));
    }

    /**
     * Get a {@link Vertex} given its unique identifier.
     *
     * @param id The unique identifier of the linked vertex to locate
     * @param <V> Framing class annotated with  {@link peapod.annotations.Vertex}
     * @throws NoSuchElementException if the linked vertex is not found.
     */
    @SuppressWarnings("unchecked")
    public <V> V v(Object id) throws NoSuchElementException {
        GraphTraversal<Vertex, Vertex> tr = graph.V(id);
        return tr.hasNext() ? frame(tr.next()) : null;
    }

    /**
     * Get a {@link Vertex} given its unique identifier.
     *
     * @param id The unique identifier of the linked vertex to locate
     * @param <V> Framing class annotated with  {@link peapod.annotations.Vertex}
     * @throws NoSuchElementException if the linked vertex is not found.
     */
    @SuppressWarnings("unchecked")
    public <V> V v(Object id, Class<V> clazz) throws NoSuchElementException {
        GraphTraversal<Vertex, Vertex> tr = graph.V(id);
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

    protected <F, E extends Element> IFramer<E, F> framer(Class<F> clazz) {
        return registry.get(clazz);
    }

    /**
     * Configure and control the transactions for those graphs that support this feature.
     * @return the transaction
     * @see com.tinkerpop.gremlin.structure.Graph#tx()
     */
    public Transaction tx() {
        return graph.tx();
    }

    /**
     * A collection of global {@link Variables} associated with the graph.
     * Variables are used for storing metadata about the graph.
     *
     * @return The variables associated with this graph
     * @see com.tinkerpop.gremlin.structure.Graph#variables()
     */
    public Variables variables() {
        return graph.variables();
    }

    /**
     * Get the {@link org.apache.commons.configuration.Configuration} associated with the construction of this graph.
     * Whatever configuration was passed to {@link com.tinkerpop.gremlin.structure.util.GraphFactory#open(org.apache.commons.configuration.Configuration)}
     * is what should be returned by this method.
     *
     * @return the configuration used during graph construction.
     * @see com.tinkerpop.gremlin.structure.Graph#configuration()
     */
    public Configuration configuration() {
        return graph.configuration();
    }

    /**
     * Gets the {@link Features} exposed by the underlying {@code Graph} implementation.
     *
     * @return a features object
     * @see com.tinkerpop.gremlin.structure.Graph#features()
     */
    public Features features() {
        return graph.features();
    }

    /**
     * @return the underlying {@link com.tinkerpop.gremlin.structure.Graph}
     */
    public Graph graph() {
        return graph;
    }

    @Override
    public void close() throws Exception {
        graph.close();
    }
}
