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

package peapod.demo.extended;

import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Vertex;

import java.util.List;

@Vertex
public interface Software extends BaseVertex  {

    public  String getName();

    public  void setName(String name);

    public  void setCreated(Integer created);

    public  String getLang();

    @In
    @Edge("created")
    public  List<Person> getCreatedBy();

    public  void addDependsOn(Software software);

    public  void addEncapsulates(Software software);
}

