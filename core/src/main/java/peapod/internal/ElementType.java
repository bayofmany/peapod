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

package peapod.internal;

import org.apache.tinkerpop.gremlin.structure.Element;

enum ElementType {

    Vertex(org.apache.tinkerpop.gremlin.structure.Vertex.class, "v"), VertexProperty(org.apache.tinkerpop.gremlin.structure.VertexProperty.class, "vp"), Edge(org.apache.tinkerpop.gremlin.structure.Edge.class, "e");

    private Class<? extends Element> clazz;
    private String field;

    ElementType(Class<? extends Element> clazz, String field) {
        this.clazz = clazz;
        this.field = field;
    }

    public Class<? extends Element> getClazz() {
        return clazz;
    }

    public String getFieldName() {
        return field;
    }
}
