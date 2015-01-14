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
public class FramedGRaphTraversal<S, E> extends DefaultTraversal<S, E> implements GraphTraversal<S, E> {

    static {
        final DefaultTraversalStrategies traversalStrategies = new DefaultTraversalStrategies();
        traversalStrategies.addStrategy(TraverserSourceStrategy.instance());
        TraversalStrategies.GlobalCache.registerStrategies(FramedGRaphTraversal.class, traversalStrategies);
    }

    private final Framer framer;

    private Class<E> lastFrameClass;

    public FramedGRaphTraversal(FramedGraph framedGraph) {
        super(framedGraph.graph());
        this.framer = framedGraph.framer();
    }

    public FramedGRaphTraversal<S, E> label(Class<E> clazz) {
        this.lastFrameClass = clazz;
        return (FramedGRaphTraversal) this.addStep(new StartStep<>(this, this.sideEffects().getGraph().V().has(T.label, clazz.getSimpleName().toLowerCase())));
    }

    protected FramedGRaphTraversal<S, E> isType(Class<E> clazz) {
        this.lastFrameClass = clazz;
        return has(T.label, clazz.getSimpleName().toLowerCase());
    }

    public FramedGRaphTraversal<S, E> has(final String key) {
        return (FramedGRaphTraversal) this.addStep(new HasStep<>(this, new HasContainer(key, Contains.within)));
    }

    public FramedGRaphTraversal<S, E> has(final String key, final Object value) {
        return (FramedGRaphTraversal) this.has(key, Compare.eq, value);
    }

    public FramedGRaphTraversal<S, E> has(final T accessor, final Object value) {
        return (FramedGRaphTraversal) this.has(accessor.getAccessor(), value);
    }

    public FramedGRaphTraversal<S, E> has(final String key, final BiPredicate predicate, final Object value) {
        return (FramedGRaphTraversal) this.addStep(new HasStep<>(this, new HasContainer(key, predicate, value)));
    }

    public FramedGRaphTraversal<S, E> has(final T accessor, final BiPredicate predicate, final Object value) {
        return (FramedGRaphTraversal) this.addStep(new HasStep<>(this, new HasContainer(accessor.getAccessor(), predicate, value)));
    }

    public FramedGRaphTraversal<S, E> has(final String label, final String key, final Object value) {
        return (FramedGRaphTraversal) this.has(label, key, Compare.eq, value);
    }

    public FramedGRaphTraversal<S, E> has(final String label, final String key, final BiPredicate predicate, final Object value) {
        return (FramedGRaphTraversal) this.has(T.label, label).addStep(new HasStep<>(this, new HasContainer(key, predicate, value)));
    }

    public FramedGRaphTraversal<S, E> hasNot(final String key) {
        return (FramedGRaphTraversal) this.addStep(new HasStep<>(this, new HasContainer(key, Contains.without)));
    }

    public FramedGRaphTraversal<S, E> values(final String... propertyKeys) {
        this.lastFrameClass = null;
        return (FramedGRaphTraversal<S, E>) this.addStep(new PropertiesStep<>(this, PropertyType.VALUE, propertyKeys));
    }

    public <E2> FramedGRaphTraversal<S, E2> in(final String edgeLabel, Class<E2> clazz) {
        FramedGRaphTraversal traversal = (FramedGRaphTraversal) this.to(Direction.IN, edgeLabel);
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