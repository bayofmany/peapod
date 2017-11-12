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
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.Traverser;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.Element;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Defines the framed graph traversals and keeps track of the traversed framed classes.
 *
 * @author Willem Salembier
 * @see org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal
 * @since 0.1
 */
@SuppressWarnings({"unchecked", "unused"})
public class FramedGraphTraversal<F> implements Iterator<F> {

    private GraphTraversal<?, ?> traversal;
    private FramedGraph graph;

    private Class<?> lastFramingClass;

    private boolean framed;

    private Map<String, Class<?>> stepLabel2FrameClass = new HashMap<>();

    FramedGraphTraversal(GraphTraversal<Vertex, Vertex> traversal, FramedGraph graph) {
        this.traversal = traversal;
        this.graph = graph;
    }

    FramedGraphTraversal<F> labels(Class<F> clazz, P<String> labels) {
        this.lastFramingClass = clazz;
        traversal.hasLabel(labels);
        return this;
    }

    public FramedGraphTraversal<F> has(final String key) {
        traversal.has(key);
        return this;
    }

    public FramedGraphTraversal<F> has(final String key, final Object value) {
        traversal.has(key, value);
        return this;
    }

    public FramedGraphTraversal<F> has(final T accessor, final Object value) {
        traversal.has(accessor, value);
        return this;
    }

    public FramedGraphTraversal<F> has(final String label, final String key, final Object value) {
        traversal.has(label, key, value);
        return this;
    }

    public FramedGraphTraversal<F> hasNot(final String key) {
        traversal.hasNot(key);
        return this;
    }

    public <E2> FramedGraphTraversal<E2> values(final String... propertyKeys) {
        this.lastFramingClass = null;
        traversal.values(propertyKeys);
        return (FramedGraphTraversal<E2>) this;
    }

    public FramedGraphTraversal<F> filter(final Predicate<Traverser<Element>> predicate) {
        traversal.filter((Predicate) predicate);
        return this;
    }

    public <F2> FramedGraphTraversal<F2> in(final String edgeLabel, Class<F2> clazz) {
        traversal.in(edgeLabel);
        this.lastFramingClass = clazz;
        return (FramedGraphTraversal<F2>) this;
    }

    public <F2> FramedGraphTraversal<F2> out(final String edgeLabel, Class<F2> clazz) {
        traversal.out(edgeLabel);
        this.lastFramingClass = clazz;
        return (FramedGraphTraversal<F2>) this;
    }

    public FramedGraphTraversal<F> out(String... edgeLabels) {
        traversal.out(edgeLabels);
        return this;
    }

    public FramedGraphTraversal<F> in(String... edgeLabels) {
        traversal.in(edgeLabels);
        return this;
    }

    public FramedGraphTraversal<F> as(final String label) {
        stepLabel2FrameClass.put(label, lastFramingClass);
        traversal.as(label);
        return this;
    }

    public <F2> FramedGraphTraversal<F2> back(final String label) {
        lastFramingClass = stepLabel2FrameClass.get(label);
        traversal.select(label);
        return (FramedGraphTraversal<F2>) this;
    }

    public FramedGraphTraversal<F> dedup() {
        traversal.dedup();
        return this;
    }

    /*public FramedGraphTraversal<S, E, F> except(String variable) {
        traversal.except(variable);
        return this;
    }

    public FramedGraphTraversal<S, E, F> except(E exceptionObject) {
        traversal.except(exceptionObject instanceof FramedElement ? (E) ((FramedElement) exceptionObject).element() : exceptionObject);
        return this;
    }

    public FramedGraphTraversal<S, E, F> except(Collection<E> exceptionCollection) {
        traversal.except(exceptionCollection.stream().map(e -> e instanceof FramedElement ? (E) ((FramedElement) e).element() : e).collect(Collectors.toList()));
        return this;
    }*/

    public List<F> toList() {
        if (lastFramingClass == null) {
            return (List<F>) traversal.toList();
        } else {
            return traversal.toList().stream().map(this::frame).collect(Collectors.toList());
        }
    }

    public Set<F> toSet() {
        if (lastFramingClass == null) {
            return (Set<F>) traversal.toList();
        } else {
            return traversal.toList().stream().map(this::frame).collect(Collectors.toSet());
        }
    }

    @Override
    public boolean hasNext() {
        return traversal.hasNext();
    }

    public F next() {
        return frame(traversal.next());
    }

    @Override
    public void remove() {
        throw new RuntimeException("Iterator::remove not supported");
    }

    @Override
    public void forEachRemaining(Consumer<? super F> action) {
        traversal.forEachRemaining(e -> action.accept(frame((Element) e)));
    }

    public Optional<F> tryNext() {
        return this.hasNext() ? Optional.of(frame(this.next())) : Optional.empty();
    }

    public FramedGraphTraversal<Long> count() {
        this.lastFramingClass = null;

        traversal.count();
        return (FramedGraphTraversal<Long>) this;
    }


    public <E2> FramedGraphTraversal<E2> properties(Class<E2> framingClass) {
        String label = graph.framer(framingClass).label();
        traversal.properties(label);
        this.lastFramingClass = framingClass;
        return (FramedGraphTraversal<E2>) this;
    }

    public <E2> FramedGraphTraversal<F> value() {
        traversal.value();
        return this;
    }

    protected F frame(Object e) {
        if (e instanceof Element) {
            return graph.frame((Element) e, (Class<F>) lastFramingClass);
        } else {
            return (F) e;
        }
    }
}