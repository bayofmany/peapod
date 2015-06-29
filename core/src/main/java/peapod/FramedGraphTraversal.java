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

import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.LambdaMapStep;
import org.apache.tinkerpop.gremlin.process.traversal.step.map.MapStep;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Defines the framed graph traversals and keeps track of the traversed framed classes.
 *
 * @author Willem Salembier
 * @see org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
 * @since 0.1
 */
@SuppressWarnings({"unchecked", "unused"})
public class FramedGraphTraversal<S, E> implements Iterator<E> {

    private GraphTraversal<S, E> traversal;
    private FramedGraph graph;

    private Class<?> lastFramingClass;

    private boolean framed;

    private Map<String, Class<?>> stepLabel2FrameClass = new HashMap<>();

    public FramedGraphTraversal(GraphTraversal traversal, FramedGraph graph) {
        this.traversal = traversal;
        this.graph = graph;
    }

    protected FramedGraphTraversal<S, E> labels(Class clazz, String[] labels) {
        this.lastFramingClass = clazz;
        traversal.hasLabel(labels);
        return this;
    }

    public FramedGraphTraversal<S, E> has(final String key) {
        traversal.has(key);
        return this;
    }

    public FramedGraphTraversal<S, E> has(final String key, final Object value) {
        traversal.has(key, value);
        return this;
    }

    public FramedGraphTraversal<S, E> has(final T accessor, final Object value) {
        traversal.has(accessor, value);
        return this;
    }

    public FramedGraphTraversal<S, E> has(final String label, final String key, final Object value) {
        traversal.has(label, key, value);
        return this;
    }

    public FramedGraphTraversal<S, E> hasNot(final String key) {
        traversal.hasNot(key);
        return this;
    }

    public <E2> FramedGraphTraversal<S, E2> values(final String... propertyKeys) {
        this.lastFramingClass = null;
        traversal.values(propertyKeys);
        return (FramedGraphTraversal<S, E2>) this;
    }

    public FramedGraphTraversal<S, E> filter(final Predicate<Traverser<E>> predicate) {
        traversal.filter(predicate);
        return this;
    }

    public <E2> FramedGraphTraversal<S, E2> in(final String edgeLabel, Class<E2> clazz) {
        traversal.in(edgeLabel);
        this.lastFramingClass = clazz;
        return (FramedGraphTraversal<S, E2>) this;
    }

    public <E2> FramedGraphTraversal<S, E2> out(final String edgeLabel, Class<E2> clazz) {
        traversal.out(edgeLabel);
        this.lastFramingClass = clazz;
        return (FramedGraphTraversal<S, E2>) this;
    }

    public FramedGraphTraversal<S, Vertex> out(String... edgeLabels) {
        traversal.out(edgeLabels);
        return (FramedGraphTraversal<S, Vertex>) this;
    }

    public FramedGraphTraversal<S, Vertex> in(String... edgeLabels) {
        traversal.in(edgeLabels);
        return (FramedGraphTraversal<S, Vertex>) this;
    }

    public FramedGraphTraversal<S, E> as(final String label) {
        stepLabel2FrameClass.put(label, lastFramingClass);
        traversal.as(label);
        return this;
    }

    public <E2> FramedGraphTraversal<S, E2> back(final String label) {
        lastFramingClass = stepLabel2FrameClass.get(label);
        traversal.select(label);
        return (FramedGraphTraversal<S, E2>) this;
    }

    public FramedGraphTraversal<S, E> dedup() {
        traversal.dedup();
        return this;
    }

    /*public FramedGraphTraversal<S, E> except(String variable) {
        traversal.except(variable);
        return this;
    }

    public FramedGraphTraversal<S, E> except(E exceptionObject) {
        traversal.except(exceptionObject instanceof FramedElement ? (E) ((FramedElement) exceptionObject).element() : exceptionObject);
        return this;
    }

    public FramedGraphTraversal<S, E> except(Collection<E> exceptionCollection) {
        traversal.except(exceptionCollection.stream().map(e -> e instanceof FramedElement ? (E) ((FramedElement) e).element() : e).collect(Collectors.toList()));
        return this;
    }*/

    public List<E> toList() {
        addFrameStep(lastFramingClass);
        return traversal.toList();
    }

    public Set<E> toSet() {
        addFrameStep(lastFramingClass);
        return traversal.toSet();
    }

    @Override
    public boolean hasNext() {
        addFrameStep(lastFramingClass);
        return traversal.hasNext();
    }

    public E next() {
        addFrameStep(lastFramingClass);
        return traversal.next();
    }

    @Override
    public void remove() {
        throw new RuntimeException("Iterator::remove not supported");
    }

    @Override
    public void forEachRemaining(Consumer<? super E> action) {
        addFrameStep(lastFramingClass);
        traversal.forEachRemaining(action);
    }

    public Optional<E> tryNext() {
        addFrameStep(lastFramingClass);
        return traversal.tryNext();
    }

    public FramedGraphTraversal<S, Long> count() {
        this.lastFramingClass = null;

        traversal.count();
        return (FramedGraphTraversal<S, Long>) this;
    }


    public <E2> FramedGraphTraversal<S, E2> properties(Class<E2> framingClass) {
        String label = graph.framer(framingClass).label();
        traversal.properties(label);
        this.lastFramingClass = framingClass;
        return (FramedGraphTraversal<S, E2>) this;
    }


    private <F> void addFrameStep(Class<F> framingClass) {
        if (framingClass == null || framed) {
            return;
        }

        MapStep<Vertex, F> mapStep = new LambdaMapStep<>(traversal.asAdmin(), v -> graph.frame(v.get(), framingClass));
        traversal.asAdmin().addStep(mapStep);
        framed = true;
    }


    public <E2> FramedGraphTraversal<S, E2> value() {
        traversal.value();
        return (FramedGraphTraversal<S, E2>) this;
    }
}