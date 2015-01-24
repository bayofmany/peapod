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

package peapod.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Marks class as a wrapper for a Tinkerpop 3 vertex.
 * <p>The wrapper class is obligatory {@code abstract}. Peapod will generate its implementation class at compile-time.</p>
 * <pre>
 * &#64;Vertex
 * public abstract class Person {
 *   public abstract String getName();
 *   public abstract void addSoftware(Software software);
 * }</pre>
 * <p>By default the class simple name is used as vertex label, but this can be changed via the annotation value.
 * The wrapper class can optionally implement {@code FramedVertex} to add traversal functionality and
 * get a reference to the wrapped {@code vertex}.</p>
 * <pre>
 * &#64;Vertex("individual")
 * public abstract class Person implements FramedVertex&lt;Person&gt; {}</pre>
 *
 * @author Willem Salembier
 * @see com.tinkerpop.gremlin.structure.Vertex
 * @see peapod.FramedVertex
 * @since 1.0
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Vertex {

    String value() default "";

}
