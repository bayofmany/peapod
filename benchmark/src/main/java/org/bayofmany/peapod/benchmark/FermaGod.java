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


import com.syncleus.ferma.AbstractVertexFrame;

public class FermaGod extends AbstractVertexFrame {
    public String getName() {
        return this.getProperty("name");
    }

    public void setName(String newName) {
        this.setProperty("name", newName);
    }

    public void removeName() {
        this.setProperty("name", null);
    }

    public Integer getAge() {
        return this.getProperty("age");
    }

    public String getType() {
        return this.getProperty("type");
    }

    public Iterable<? extends FermaGod> getSons() {
        return traverse((v) -> v.in("father")).toList(FermaGod.class);
    }

    Iterable<? extends FermaGod> getParents() {
        return traverse((v) -> v.out("father")).toList(FermaGod.class);
    }
}
