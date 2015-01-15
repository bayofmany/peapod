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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import javax.tools.JavaFileObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.google.common.truth.Truth.assert_;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

/**
 * Created by Willem on 28/12/2014.
 */
@RunWith(Parameterized.class)
public class AnnotationProcessorTest {

    @Rule
    public CompilationRule compilationRule = new CompilationRule();

    @Parameters(name = "{index}: compile peapod/testcases/{0}/{1}.java")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {"property", new String[]{"Person"}},
                {"hiddenproperty", new String[]{"Person"}},
                {"outvertex", new String[]{"Person"}},
                {"outedge", new String[]{"Person", "Knows"}}
        });
    }

    private String packageName;
    private String[] classNames;

    public AnnotationProcessorTest(String packageName, String[] classNames) {
        this.packageName = packageName;
        this.classNames = classNames;
    }

    @Test
    public void testCompile() {
        List<JavaFileObject> input = new ArrayList<>();
        JavaFileObject[] output = new JavaFileObject[classNames.length];
        int i = 0;
        for (String className : classNames) {
            input.add(JavaFileObjects.forResource("peapod/testcases/" + packageName + "/" + className + ".java"));
            output[i] = JavaFileObjects.forResource("peapod/testcases/" + packageName + "/" + classNames[i] + "$Impl.java");
            i++;
        }

        assert_().about(javaSources())
                .that(input)
                .processedWith(new AnnotationProcessor())
                .compilesWithoutError()
                .and().generatesSources(output[0], output);
    }
}
