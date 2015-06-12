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

import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.ArrayList;
import java.util.List;

public class TinkerPopHelper {

    public static List<Vertex> out(Vertex vertex, String label) {
        List<Vertex> result = new ArrayList<>();
        vertex.edges(Direction.OUT, label).forEachRemaining(it -> result.add(it.inVertex()));
        return result;
    }

    public static List<Vertex> in(Vertex vertex, String label) {
        List<Vertex> result = new ArrayList<>();
        vertex.edges(Direction.IN, label).forEachRemaining(it -> result.add(it.outVertex()));
        return result;
    }


}
