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

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * <p>Marks class as a wrapper class for a TinkerPop 3 vertex property. The wrapper class is obligatory {@code abstract}.
 * Peapod will generate its implementation class at compile-time.</p>
 * <p>Vertex property classes should only be created when the TinkerPop 3 graph database supports meta-properties.</p>
 * <p>Additionally, the class must implement the {@code FramedVertexProperty} interface and specify the property type via
 * the type argument. Hence, the vertex property values can be set with the {@link peapod.FramedVertexProperty#getValue}
 * and {@link peapod.FramedVertexProperty#setValue} methods.</p>
 * <pre>
 * &#64;VertexProperty
 * public abstract class Name implements FramedVertexProperty&lt;String&gt; {
 *   public abstract String getAcl();
 *   public abstract void setAcl(String acl);
 * }</pre>
 * <p>By default the class simple name will define the property name, but this can be changed via the annotation value.</p>
 * <pre>
 * &#64;VertexProperty("denomination")
 * public abstract class Name implements FramedVertexProperty&lt;String&gt; {}</pre>
 *
 * @author Willem Salembier
 * @see org.apache.tinkerpop.gremlin.structure.VertexProperty
 * @see peapod.FramedVertexProperty
 * @see org.apache.tinkerpop.gremlin.structure.Graph.Features.VertexFeatures#supportsMetaProperties()
 * @since 0.1
 */
@Retention(RUNTIME)
@Target({TYPE})
public @interface VertexProperty {

    String value() default "";
}
