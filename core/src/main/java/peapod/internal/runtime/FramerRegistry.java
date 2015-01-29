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

package peapod.internal.runtime;

import com.tinkerpop.gremlin.structure.Element;
import peapod.annotations.Edge;
import peapod.annotations.Vertex;
import peapod.annotations.VertexProperty;

import java.util.HashMap;
import java.util.Map;

public class FramerRegistry {

    private final Map<String, IFramer<?, ?>> vertexFramers = new HashMap<>();
    private final Map<String, IFramer<?, ?>> vertexPropertyFramers = new HashMap<>();
    private final Map<String, IFramer<?, ?>> edgeFramers = new HashMap<>();

    private final Map<Class<?>, IFramer<?, ?>> framers = new HashMap<>();

    @SuppressWarnings("unchecked")
    private <E extends Element, F> IFramer<E, F> register(Class<F> framed) {
        try {
            Class<?> framingClass = framed.getClassLoader().loadClass(framed.getName() + "$Impl");
            IFramer<E, F> framer = (IFramer<E, F>) framingClass.getField("instance").get(null);

            if (framed.getAnnotation(Vertex.class) != null) {
                vertexFramers.put(framer.label(), framer);
            } else if (framed.getAnnotation(Edge.class) != null) {
                edgeFramers.put(framer.label(), framer);
            } else if (framed.getAnnotation(VertexProperty.class) != null) {
                vertexPropertyFramers.put(framer.label(), framer);
            }

            framers.put(framed, framer);
            return framer;
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public <E extends Element, F> IFramer<E, F> get(E e, Class<F> framingClass) {
        IFramer<E, F> framer = get(e);
        return framer == null ? get(framingClass) : framer;
    }

    @SuppressWarnings("unchecked")
    public <E extends Element, F> IFramer<E, F> get(Class<F> framingClass) {
        return (IFramer<E, F>) framers.getOrDefault(framingClass, register(framingClass));
    }

    @SuppressWarnings("unchecked")
    public <E extends Element, F> IFramer<E, F> get(E e) {
        if (e instanceof com.tinkerpop.gremlin.structure.Vertex) {
            return (IFramer<E, F>) vertexFramers.get(e.label());
        } else if (e instanceof com.tinkerpop.gremlin.structure.Edge) {
            return (IFramer<E, F>) edgeFramers.get(e.label());
        } else if (e instanceof com.tinkerpop.gremlin.structure.VertexProperty) {
            return (IFramer<E, F>) vertexPropertyFramers.get(e.label());
        }
        return null;
    }
}
