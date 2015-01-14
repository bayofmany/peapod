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

package peapod.demo.classic;

import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;

/**
 * Created by Willem on 1/01/2015.
 */
@Edge(label = "knows")
public abstract class Knows {

    public abstract Float getWeight();

    @Out
    public abstract Person getPerson();

    @In
    public abstract Person getOtherPerson();

}
