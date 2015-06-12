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

package peapod.internal.runtime;

import org.apache.tinkerpop.gremlin.structure.Element;

import java.util.*;

public class FramerRegistry {

    private final Map<String, IFramer<?, ?>> vertexFramers = new HashMap<>();
    private final Map<String, IFramer<?, ?>> vertexPropertyFramers = new HashMap<>();
    private final Map<String, IFramer<?, ?>> edgeFramers = new HashMap<>();

    private final Map<Class<?>, IFramer<?, ?>> framers = new HashMap<>();

    private final Map<Class<?>, List<String>> class2Labels = new HashMap<>();


    @SuppressWarnings("unchecked")
    public void register(Set<Class<?>> classes) {
        classes.forEach(c -> {
            try {
                IFramer framer = (IFramer) c.newInstance();
                framers.put(framer.frameClass(), framer);
                if (org.apache.tinkerpop.gremlin.structure.Vertex.class.equals(framer.type())) {
                    vertexFramers.put(framer.label(), framer);
                } else if (org.apache.tinkerpop.gremlin.structure.Edge.class.equals(framer.type())) {
                    edgeFramers.put(framer.label(), framer);
                } else if (org.apache.tinkerpop.gremlin.structure.VertexProperty.class.equals(framer.type())) {
                    vertexPropertyFramers.put(framer.label(), framer);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });

        framers.values().forEach(f -> {
            String label = f.label();
            Class<?> aClass = f.frameClass();
            while (!Object.class.equals(aClass)) {
                class2Labels.computeIfAbsent(aClass, c -> new ArrayList<>()).add(label);
                aClass = aClass.getSuperclass();
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <E extends Element, F> IFramer<E, F> get(E e, Class<F> clazz) {
        IFramer<E, F> framer = null;
        if (e instanceof org.apache.tinkerpop.gremlin.structure.Vertex) {
            framer = (IFramer<E, F>) vertexFramers.get(e.label());
        } else if (e instanceof org.apache.tinkerpop.gremlin.structure.Edge) {
            framer = (IFramer<E, F>) edgeFramers.get(e.label());
        } else if (e instanceof org.apache.tinkerpop.gremlin.structure.VertexProperty) {
            framer = (IFramer<E, F>) vertexPropertyFramers.get(e.label());
        }
        if (framer == null) {
            framer = (IFramer<E, F>) framers.get(clazz);
        }
        if (framer == null) {
            throw new RuntimeException("No framer found for " + e.getClass().getSimpleName() + " with label " + e.label());
        }
        return framer;
    }

    @SuppressWarnings("unchecked")
    public <E extends Element, F> IFramer<E, F> get(Class<F> clazz) {
        IFramer<E, F> framer = (IFramer<E, F>) framers.get(clazz);
        if (framer == null) {
            throw new RuntimeException("No framer found for " + clazz);
        }
        return framer;
    }

    public <V> Collection<String> labels(Class<V> clazz) {
        return class2Labels.get(clazz);
    }

}
