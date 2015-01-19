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

package peapod.impl;

import com.google.testing.compile.CompilationRule;
import com.google.testing.compile.JavaFileObjects;
import org.junit.Rule;
import org.junit.Test;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.List;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class AnnotationProcessorTest {

    @Rule
    public CompilationRule compilationRule = new CompilationRule();

    @Test
    public void testCompile() {
        List<JavaFileObject> input = new ArrayList<>();
        input.add(JavaFileObjects.forResource("peapod/impl/Person.java"));
        input.add(JavaFileObjects.forResource("peapod/impl/Knows.java"));
        JavaFileObject framedVertex = JavaFileObjects.forResource("peapod/impl/Person$Impl.java");
        JavaFileObject framedEdge = JavaFileObjects.forResource("peapod/impl/Knows$Impl.java");

        assert_().about(javaSources())
                .that(input)
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError()
                .and().generatesSources(framedVertex, framedEdge);
    }
}
