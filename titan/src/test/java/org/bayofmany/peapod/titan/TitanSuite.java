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

package org.bayofmany.peapod.titan;

import com.thinkaurelius.titan.core.TitanFactory;
import com.tinkerpop.gremlin.structure.Graph;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.rules.TemporaryFolder;
import peapod.GraphProvider;
import peapod.GraphTest;
import peapod.GraphTestSuite;

import java.io.IOException;

@Ignore("Titan 0.9.0-M1 follows Tinkerpop 3.0.0.M6")
public class TitanSuite extends GraphTestSuite {

    @ClassRule
    public static TemporaryFolder folder = new TemporaryFolder();

    @BeforeClass
    public static void setGraphProvider() throws IOException {
        GraphTest.graphProvider = new GraphProvider() {
            @Override
            public Graph getGraph() throws IOException {
                return TitanFactory.build().set("storage.backend", "inmemory").open();
            }
        };
    }

}
