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

import com.tinkerpop.gremlin.structure.Vertex;

/**
 * <p>All generated {@code @Vertex} classes implement the {@code FramedVertex} interface. It can be optionally
 * defined on the abstract class for traversals and to get a reference to the wrapped Tinkerpop 3 {@code vertex}.</p>
 * <pre>
 * &#64;Vertex
 * public abstract class Person implements FramedVertex&lt;Person&gt; {}</pre>
 *
 * @author Willem Salembier
 * @see com.tinkerpop.gremlin.structure.Vertex
 * @since 0.1
 */
public interface FramedVertex<V> extends FramedElement, FramedVertexTraversal<V> {

    default Vertex vertex() {
        return (Vertex) element();
    }

    @SuppressWarnings("unchecked")
    default FramedGraphTraversal<V, V> start() {
        return new FramedGraphTraversal(vertex().start(), graph());
    }


}
