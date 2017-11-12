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

package org.bayofmany.peapod.benchmark;

import com.syncleus.ferma.VertexFrame;
import com.syncleus.ferma.annotations.Adjacency;
import com.syncleus.ferma.annotations.Incidence;
import com.syncleus.ferma.annotations.Property;

import java.util.Iterator;

/**
 * Created by Willem on 2/01/2017.
 */
public abstract class Person implements VertexFrame {
    @Property("name")
    public abstract String getName();

    @Property("name")
    public abstract void setName(String name);

    @Adjacency(label = "knows")
    public abstract Iterator<Person> getKnowsPeople();

    @Incidence(label = "knows")
    public abstract Iterator<Knows> getKnows();

    @Incidence(label = "knows")
    public abstract Knows addKnows(Person friend);

}