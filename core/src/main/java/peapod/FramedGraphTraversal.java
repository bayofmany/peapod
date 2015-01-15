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
import com.tinkerpop.gremlin.process.Traverser;
import com.tinkerpop.gremlin.process.graph.step.map.MapStep;
import com.tinkerpop.gremlin.process.graph.step.sideEffect.StartStep;
import com.tinkerpop.gremlin.process.graph.strategy.TraverserSourceStrategy;
import com.tinkerpop.gremlin.process.util.DefaultTraversalStrategies;
import com.tinkerpop.gremlin.structure.Vertex;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

/**
 * Extension of {@link com.tinkerpop.gremlin.process.Traversal} supporting framed vertices and edges.
 */
@SuppressWarnings("unchecked")
public class FramedGraphTraversal<S, E> extends DefaultGraphTraversal<S, E> {

    static {
        final DefaultTraversalStrategies traversalStrategies = new DefaultTraversalStrategies();
        traversalStrategies.addStrategy(TraverserSourceStrategy.instance());
        TraversalStrategies.GlobalCache.registerStrategies(FramedGraphTraversal.class, traversalStrategies);
    }

    private final Framer framer;

    private Class<E> lastFrameClass;

    private Map<String, Class<E>> stepLabel2FrameClass = new HashMap<>();

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
        return this;//has(T.label, clazz.getSimpleName().toLowerCase());
    }

    public FramedGraphTraversal<S, E> has(final String key) {
        return (FramedGraphTraversal) super.has(key);
    }

    public FramedGraphTraversal<S, E> has(final String key, final Object value) {
        return (FramedGraphTraversal) super.has(key, value);
    }

    public FramedGraphTraversal<S, E> has(final T accessor, final Object value) {
        return (FramedGraphTraversal) super.has(accessor, value);
    }

    public FramedGraphTraversal<S, E> has(final String key, final BiPredicate predicate, final Object value) {
        return (FramedGraphTraversal) super.has(key, predicate, value);
    }

    public FramedGraphTraversal<S, E> has(final T accessor, final BiPredicate predicate, final Object value) {
        return (FramedGraphTraversal) super.has(accessor, predicate, value);
    }

    public FramedGraphTraversal<S, E> has(final String label, final String key, final Object value) {
        return (FramedGraphTraversal) super.has(label, key, value);
    }

    public FramedGraphTraversal<S, E> has(final String label, final String key, final BiPredicate predicate, final Object value) {
        return (FramedGraphTraversal) super.has(label, key, predicate, value);
    }

    public FramedGraphTraversal<S, E> hasNot(final String key) {
        return (FramedGraphTraversal) super.hasNot(key);
    }

    public FramedGraphTraversal<S, E> values(final String... propertyKeys) {
        this.lastFrameClass = null;
        return (FramedGraphTraversal<S, E>) super.values(propertyKeys);
    }

    public FramedGraphTraversal<S, E> filter(final Predicate<Traverser<E>> predicate) {
        return (FramedGraphTraversal<S, E>) super.filter(predicate);
    }

    public <E2> FramedGraphTraversal<S, E2> in(final String edgeLabel, Class<E2> clazz) {
        FramedGraphTraversal traversal = (FramedGraphTraversal) super.in(edgeLabel);
        return traversal.isType(clazz);
    }

    public <E2> FramedGraphTraversal<S, E2> out(final String edgeLabel, Class<E2> clazz) {
        FramedGraphTraversal traversal = (FramedGraphTraversal) super.out(edgeLabel);
        return traversal.isType(clazz);

    }

    public FramedGraphTraversal<S, E> as(final String label) {
        stepLabel2FrameClass.put(label, lastFrameClass);
        return (FramedGraphTraversal<S, E>) super.as(label);
    }

    public FramedGraphTraversal<S, E> back(final String label) {
        lastFrameClass = stepLabel2FrameClass.get(label);
        return (FramedGraphTraversal<S, E>) super.back(label);
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