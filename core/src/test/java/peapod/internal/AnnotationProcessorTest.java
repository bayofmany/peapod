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

package peapod.internal;

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
    public void testCompileForAbstractClasses() {
        List<JavaFileObject> input = new ArrayList<>();
        input.add(JavaFileObjects.forResource("peapod/internal/Person.java"));
        input.add(JavaFileObjects.forResource("peapod/internal/Knows.java"));
        JavaFileObject framedVertex = JavaFileObjects.forResource("peapod/internal/Person$Impl.java");
        JavaFileObject framedEdge = JavaFileObjects.forResource("peapod/internal/Knows$Impl.java");

        assert_().about(javaSources())
                .that(input)
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError()
                .and().generatesSources(framedVertex, framedEdge);
    }

    @Test
    public void testCompileForInterfaces() {
        List<JavaFileObject> input = new ArrayList<>();
        input.add(JavaFileObjects.forResource("peapod/internal/PersonInterface.java"));
        input.add(JavaFileObjects.forResource("peapod/internal/KnowsInterface.java"));
        input.add(JavaFileObjects.forResource("peapod/internal/ProgrammerInterface.java"));
        //JavaFileObject framedVertex = JavaFileObjects.forResource("peapod/internal/Person$Impl.java");

        assert_().about(javaSources())
                .that(input)
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError();
                //.and().generatesSources(framedVertex);
    }
}
