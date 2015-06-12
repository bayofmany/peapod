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

import org.apache.tinkerpop.gremlin.structure.Vertex;

/**
 * Defines the traversals supported on framed vertices.
 *
 * @author Willem Salembier
 * @since 0.1
 */
@SuppressWarnings({"unchecked", "unused"})
public interface FramedVertexTraversal<S> {

    public FramedGraphTraversal<S, S> start();

    public default FramedGraphTraversal<S, Vertex> out(final String... edgeLabel) {
        return start().out(edgeLabel);
    }

    public default <E> FramedGraphTraversal<S, E> out(final String edgeLabel, Class<E> clazz) {
        return start().out(edgeLabel, clazz);
    }

    public default FramedGraphTraversal<S, Vertex> in(final String... edgeLabel) {
        return start().in(edgeLabel);
    }

    public default <E2> FramedGraphTraversal<S, E2> in(final String edgeLabel, Class<E2> clazz) {
        return start().in(edgeLabel, clazz);
    }

    public default <E2> FramedGraphTraversal<S, E2> properties(Class<E2> clazz) {
        return this.start().properties(clazz);
    }

}
