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

package peapod.internal;

import com.tinkerpop.gremlin.structure.Element;
import peapod.FramedGraph;

import java.util.Collection;

/**
 * A Framer implementation converts Tinkerpop {@link com.tinkerpop.gremlin.structure.Element} instance to framed objects.
 */
public interface IFramer<E extends Element, F> {

    String label();

    Collection<String> subLabels();

    public F frame(E element, FramedGraph framedGraph);
}
