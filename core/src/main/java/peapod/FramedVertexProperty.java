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

import peapod.annotations.VertexProperty;

/**
 * <p>All vertex property wrapper classes must obligatory implement the {@code FramedVertexProperty} interface.
 * Via the interface type argument the developer specifies the vertex porperty type.</p>
 * <pre>
 * &#64;VertexProperty
 * public abstract class Name implements FramedVertexProperty&lt;String&gt; {
 * }</pre>
 *
 * @author Willem Salembier
 * @see org.apache.tinkerpop.gremlin.structure.VertexProperty
 * @see peapod.annotations.VertexProperty
 * @since 0.1
 */
public interface FramedVertexProperty<T> extends FramedElement {

    void setValue(T t);

    T getValue();

    default VertexProperty property() {
        return (VertexProperty) element();
    }

}
