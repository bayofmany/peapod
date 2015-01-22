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

import com.tinkerpop.gremlin.structure.Element;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

class FramerRegistry {

    static final FramerRegistry instance = new FramerRegistry();

    private final Map<Class<?>, Framer<?, ?>> framers = new HashMap<>();

    @SuppressWarnings("unchecked")
    private <E extends Element, F> Framer<E, F> register(Class<F> framed) {
        try {
            Class<?> framingClass = framed.getClassLoader().loadClass(framed.getName() + "$Impl");
            Framer<E, F> framer = (Framer<E, F>) framingClass.getMethod("framer").invoke(null);
            framers.put(framed, framer);
            return framer;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    <E extends Element, F> Framer<E, F> get(Class<F> framed) {
        return (Framer<E, F>) framers.getOrDefault(framed, register(framed));
    }

}
