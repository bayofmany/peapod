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

package org.bayofmany.peapod.titan;

import com.thinkaurelius.titan.core.Cardinality;
import com.thinkaurelius.titan.core.TitanFactory;
import com.thinkaurelius.titan.core.TitanGraph;
import com.thinkaurelius.titan.core.schema.TitanManagement;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.rules.TemporaryFolder;
import peapod.GraphProvider;
import peapod.GraphTest;
import peapod.GraphTestSuite;

import java.io.IOException;

public class TitanSuite extends GraphTestSuite {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setGraphProvider() throws IOException {
        GraphTest.graphProvider = new GraphProvider() {
            public Graph getGraph() throws IOException {
                TitanGraph graph = TitanFactory.build().set("storage.backend", "inmemory").open();
                TitanManagement management = graph.openManagement();
                management.makePropertyKey("location").dataType(String.class).cardinality(Cardinality.LIST).make();
                management.makePropertyKey("firstName").dataType(String.class).cardinality(Cardinality.LIST).make();
                management.commit();
                return graph;
            }
        };

    }

}
