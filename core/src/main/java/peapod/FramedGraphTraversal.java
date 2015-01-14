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
import com.tinkerpop.gremlin.process.TraversalStrategies;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import com.tinkerpop.gremlin.process.graph.step.filter.HasStep;
import com.tinkerpop.gremlin.process.graph.step.map.MapStep;
import com.tinkerpop.gremlin.process.graph.step.map.PropertiesStep;
import com.tinkerpop.gremlin.process.graph.step.sideEffect.StartStep;
import com.tinkerpop.gremlin.process.graph.strategy.TraverserSourceStrategy;
import com.tinkerpop.gremlin.process.util.DefaultTraversal;
import com.tinkerpop.gremlin.process.util.DefaultTraversalStrategies;
import com.tinkerpop.gremlin.structure.*;
import com.tinkerpop.gremlin.structure.Direction;
import com.tinkerpop.gremlin.structure.util.HasContainer;

import java.util.List;
import java.util.Set;
import java.util.function.BiPredicate;

/**
 * Extension of {@link com.tinkerpop.gremlin.process.Traversal} supporting framed vertices and edges.
 */
@SuppressWarnings("unchecked")
public class FramedGraphTraversal<S, E> extends DefaultTraversal<S, E> implements GraphTraversal<S, E> {

    static {
        final DefaultTraversalStrategies traversalStrategies = new DefaultTraversalStrategies();
        traversalStrategies.addStrategy(TraverserSourceStrategy.instance());
        TraversalStrategies.GlobalCache.registerStrategies(FramedGraphTraversal.class, traversalStrategies);
    }

    private final Framer framer;

    private Class<E> lastFrameClass;

    public FramedGraphTraversal(FramedGraph framedGraph) {
        super(framedGraph.graph());
        this.framer = framedGraph.framer();
    }

    public FramedGraphTraversal<S, E> label(Class<E> clazz) {
        this.lastFrameClass = clazz;
        return (FramedGraphTraversal) this.addStep(new StartStep<>(this, this.sideEffects().getGraph().V().has(T.label, clazz.getSimpleName().toLowerCase())));
    }

    protected FramedGraphTraversal<S, E> isType(Class<E> clazz) {
        this.lastFrameClass = clazz;
        return has(T.label, clazz.getSimpleName().toLowerCase());
    }

    public FramedGraphTraversal<S, E> has(final String key) {
        return (FramedGraphTraversal) this.addStep(new HasStep<>(this, new HasContainer(key, Contains.within)));
    }

    public FramedGraphTraversal<S, E> has(final String key, final Object value) {
        return (FramedGraphTraversal) this.has(key, Compare.eq, value);
    }

    public FramedGraphTraversal<S, E> has(final T accessor, final Object value) {
        return (FramedGraphTraversal) this.has(accessor.getAccessor(), value);
    }

    public FramedGraphTraversal<S, E> has(final String key, final BiPredicate predicate, final Object value) {
        return (FramedGraphTraversal) this.addStep(new HasStep<>(this, new HasContainer(key, predicate, value)));
    }

    public FramedGraphTraversal<S, E> has(final T accessor, final BiPredicate predicate, final Object value) {
        return (FramedGraphTraversal) this.addStep(new HasStep<>(this, new HasContainer(accessor.getAccessor(), predicate, value)));
    }

    public FramedGraphTraversal<S, E> has(final String label, final String key, final Object value) {
        return (FramedGraphTraversal) this.has(label, key, Compare.eq, value);
    }

    public FramedGraphTraversal<S, E> has(final String label, final String key, final BiPredicate predicate, final Object value) {
        return (FramedGraphTraversal) this.has(T.label, label).addStep(new HasStep<>(this, new HasContainer(key, predicate, value)));
    }

    public FramedGraphTraversal<S, E> hasNot(final String key) {
        return (FramedGraphTraversal) this.addStep(new HasStep<>(this, new HasContainer(key, Contains.without)));
    }

    public FramedGraphTraversal<S, E> values(final String... propertyKeys) {
        this.lastFrameClass = null;
        return (FramedGraphTraversal<S, E>) this.addStep(new PropertiesStep<>(this, PropertyType.VALUE, propertyKeys));
    }

    public <E2> FramedGraphTraversal<S, E2> in(final String edgeLabel, Class<E2> clazz) {
        FramedGraphTraversal traversal = (FramedGraphTraversal) this.to(Direction.IN, edgeLabel);
        return traversal.isType(clazz);
    }

    @Override
    public List<E> toList() {
        addFrameStep(lastFrameClass);
        return super.toList();
    }

    @Override
    public Set<E> toSet() {
        addFrameStep(lastFrameClass);
        return super.toSet();
    }

    @Override
    public E next() {
        addFrameStep(lastFrameClass);
        return super.next();
    }

    private void addFrameStep(Class<E> clazz) {
        if (clazz == null) {
            return;
        }

        MapStep<Vertex, E> mapStep = new MapStep<>(this);
        mapStep.setFunction(v -> framer.frame(clazz, v.get()));
        this.addStep(mapStep);
    }

}