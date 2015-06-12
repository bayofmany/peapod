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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.VertexProperty;
import peapod.FramedVertex;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FrameHelper {

    public static void removeEdge(Vertex start, Direction direction, String label, Vertex end) {
        if (direction == Direction.OUT) {
            start.edges(direction, label).forEachRemaining(e -> {
                if (e.inVertex().equals(end)) {
                    e.remove();
                }
            });
        } else if (direction == Direction.IN) {
            start.edges(direction, label).forEachRemaining(e -> {
                if (e.outVertex().equals(end)) {
                    e.remove();
                }
            });
        }
    }

    public static <T> T filterEdge(FramedVertex framedVertex, String label, FramedVertex link, Class<T> framedClass) {
        if (link == null) {
            Iterator<Edge> it = framedVertex.vertex().edges(Direction.OUT, label);
            return it.hasNext() ? framedVertex.graph().frame(it.next(), framedClass) : null;
        } else {
            Iterator<Edge> it = framedVertex.vertex().edges(Direction.OUT, label);
            while (it.hasNext()) {
                Edge edge = it.next();
                if (edge.inVertex().equals(link.vertex())) {
                    return framedVertex.graph().frame(edge, framedClass);
                }
            }
            return null;
        }
    }

    public static <T> List<T> getLinkedVertices(FramedVertex framedVertex, Direction direction, String label, Class<T> frameClass) {
        return framedVertex.graph().frame(framedVertex.vertex().vertices(direction, label), frameClass);
    }

    public static <V, F> F filterVertexProperty(FramedVertex framedVertex, String label, V value, Class<F> frameClass) {
        if (value == null) {
            throw new IllegalArgumentException("Filter value is <null>");
        }

        Iterator<VertexProperty<V>> it = framedVertex.vertex().properties(label);
        while (it.hasNext()) {
            VertexProperty<V> vertexProperty = it.next();
            if (value.equals(vertexProperty.value())) {
                return framedVertex.graph().frame(vertexProperty, frameClass);
            }
        }

        return null;
    }

    public static <V> List<V> toList(Iterator<V> it) {
        List<V> result = new ArrayList<>();
        it.forEachRemaining(result::add);
        return result;
    }

    public static <V> void removeVertexProperty(FramedVertex framedVertex, String label, V value) {
        if (value == null) {
            throw new IllegalArgumentException("Filter value is <null>");
        }

        Iterator<VertexProperty<V>> it = framedVertex.vertex().properties(label);
        while (it.hasNext()) {
            VertexProperty<V> vertexProperty = it.next();
            if (value.equals(vertexProperty.value())) {
                vertexProperty.remove();
                return;
            }
        }
    }
}
