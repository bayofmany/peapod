/*
 * Copyright 2015-Bay of Many
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
 * This project is derived from code in the Tinkerpop project under the following licenses:
 *
 * Tinkerpop3
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the TinkerPop nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL TINKERPOP BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package peapod.impl;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import java.util.*;

public class ClassDescription extends BaseDescription {

    private String packageName;

    private Map<ExecutableElement, BaseDescription> method2Description = new LinkedHashMap<>();

    private Map<String, BaseDescription> name2Description = new HashMap<>();

    private Set<String> imports = new HashSet<>();

    public ClassDescription(TypeElement t) {
        packageName = ((PackageElement) t.getEnclosingElement()).getQualifiedName().toString();
    }


    public BaseDescription getDescription(String name) {
        return name2Description.get(name);
    }

    public void setDescription(String property, ExecutableElement element, BaseDescription descr) {
        name2Description.put(property, descr);
        method2Description.put(element, descr);
        addImport(descr.getType());
    }

    public Set<ExecutableElement> getMethods() {
        return method2Description.keySet();
    }

    public BaseDescription getDescription(ExecutableElement method) {
        return method2Description.get(method);
    }

    public Set<String> getImports() {
        return imports;
    }

    public void addImport(String anImport) {
        if (!anImport.startsWith("java.lang.") && !anImport.startsWith(packageName)) {
            imports.add(anImport);
        }
    }

    public void addImport(TypeMirror type) {
        if (type != null && !type.getKind().isPrimitive()) {
            addImport(type.toString());
        }
    }
}