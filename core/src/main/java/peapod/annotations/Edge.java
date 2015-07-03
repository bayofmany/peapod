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

package peapod.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Marks class as a wrapper class for a TinkerPop 3 edge. The wrapper class is obligatory {@code abstract}.
 * Peapod will generate its implementation class at compile-time.</p>
 * <pre>
 * &#64;Edge
 * public abstract class Develops {
 *
 *   public abstract String getName();
 *
 *   &#64;Out
 *   public abstract Person getPerson();
 *
 *   &#64;In
 *   public abstract Software getSoftware();
 * }</pre>
 * <p>By default the class simple name in lower case is used as edge label, but this can be changed via the annotation value.
 * <p>The wrapper class can optionally implement {@code FramedEdge} to get a reference to the wrapped {@code edge}.</p>
 * <pre>
 * &#64;Edge("has_developed")
 * public abstract class Develops implements FramedEdge {}</pre>
 *
 * @author Willem Salembier
 * @see org.apache.tinkerpop.gremlin.structure.Edge
 * @see peapod.FramedEdge
 * @since 0.1
 */
@Retention(RUNTIME)
@Target({TYPE, METHOD})
public @interface Edge {

    String value() default "";

}
