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

public class FramerRegistry {

    protected static final FramerRegistry instance = new FramerRegistry();

    private Map<Class<?>, Framer<?, ?>> framers = new HashMap<>();

    private <F, E extends Element> Framer<F, E> register(Class<F> framed) {
        try {
            Class<?> framingClass = framed.getClassLoader().loadClass(framed.getName() + "$Impl");
            Framer<F, E> framer = (Framer<F, E>) framingClass.getMethod("framer").invoke(null);
            framers.put(framed, framer);
            return framer;
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    protected <F, E extends Element> Framer<F, E> get(Class<F> framed) {
        return (Framer<F, E>) framers.getOrDefault(framed, register(framed));
    }

}
