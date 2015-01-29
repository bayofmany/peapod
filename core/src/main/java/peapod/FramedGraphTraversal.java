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

package peapod;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.step.map.MapStep;
import com.tinkerpop.gremlin.structure.Contains;
import com.tinkerpop.gremlin.structure.Vertex;

import java.util.*;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Defines the framed graph traversals and keeps track of the traversed framed classes.
 *
 * @author Willem Salembier
 * @see com.tinkerpop.gremlin.process.graph.GraphTraversal
 * @since 0.1
 */
@SuppressWarnings({"unchecked", "unused"})
public class FramedGraphTraversal<S, E> {

    private GraphTraversal<S, E> traversal;
    private FramedGraph graph;

    private Class<?> lastFramingClass;

    private Map<String, Class<?>> stepLabel2FrameClass = new HashMap<>();

    public FramedGraphTraversal(GraphTraversal traversal, FramedGraph graph) {
        this.traversal = traversal;
        this.graph = graph;
    }

    protected FramedGraphTraversal<S, E> labels(Class clazz, Collection<String> labels) {
        this.lastFramingClass = clazz;
        traversal.has(T.label, Contains.within, labels);
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

    public FramedGraphTraversal<S, E> has(final String key, final BiPredicate predicate, final Object value) {
        traversal.has(key, predicate, value);
        return this;
    }

    public FramedGraphTraversal<S, E> has(final T accessor, final BiPredicate predicate, final Object value) {
        traversal.has(accessor, predicate, value);
        return this;
    }

    public FramedGraphTraversal<S, E> has(final String label, final String key, final Object value) {
        traversal.has(label, key, value);
        return this;
    }

    public FramedGraphTraversal<S, E> has(final String label, final String key, final BiPredicate predicate, final Object value) {
        traversal.has(label, key, predicate, value);
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
        traversal.back(label);
        return (FramedGraphTraversal<S, E2>) this;
    }

    public FramedGraphTraversal<S, E> dedup() {
        traversal.dedup();
        return this;
    }

    public FramedGraphTraversal<S, E> except(String variable) {
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
    }

    public List<E> toList() {
        addFrameStep(lastFramingClass);
        return traversal.toList();
    }

    public Set<E> toSet() {
        addFrameStep(lastFramingClass);
        return traversal.toSet();
    }

    public E next() {
        addFrameStep(lastFramingClass);
        return traversal.next();
    }

    public Optional<E> tryNext() {
        addFrameStep(lastFramingClass);
        return traversal.tryNext();
    }

    public FramedGraphTraversal<S, Long> count() {
        this.lastFramingClass = null;

        GraphTraversal<S, Long> count = traversal.count();
        return (FramedGraphTraversal<S, Long>) this;
    }


    public <E2> FramedGraphTraversal<S, E2> properties(Class<E2> framingClass) {
        String label = graph.framer(framingClass).label();
        traversal.properties(label);
        this.lastFramingClass = framingClass;
        return (FramedGraphTraversal<S, E2>) this;
    }


    private <F> void addFrameStep(Class<F> framingClass) {
        if (framingClass == null) {
            return;
        }

        MapStep<Vertex, F> mapStep = new MapStep<>(traversal);
        mapStep.setFunction(v -> graph.frame(v.get(), framingClass));
        traversal.asAdmin().addStep(mapStep);
    }


    public <E2> FramedGraphTraversal<S, E2> value() {
        traversal.value();
        return (FramedGraphTraversal<S, E2>) this;
    }
}