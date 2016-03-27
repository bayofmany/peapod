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

import com.squareup.javapoet.*;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversal;
import org.apache.tinkerpop.gremlin.structure.T;
import peapod.*;
import peapod.annotations.*;
import peapod.internal.runtime.DefaultIterable;
import peapod.internal.runtime.FrameHelper;
import peapod.internal.runtime.Framer;
import peapod.internal.runtime.IFramer;

import javax.annotation.PostConstruct;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.tools.Diagnostic.Kind.*;
import static javax.tools.Diagnostic.Kind.OTHER;
import static peapod.internal.Direction.*;

/**
 * Annotation processor for all {link @Vertex} annotated classes that generates the concrete implementation classes.
 */
@SupportedAnnotationTypes({"peapod.annotations.Vertex", "peapod.annotations.VertexProperty", "peapod.annotations.Edge"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class AnnotationProcessor extends AbstractProcessor {

    private Messager messager;

    private Filer filer;

    private Types types;

    @Override
    public void init(final ProcessingEnvironment environment) {
        super.init(environment);
        this.messager = environment.getMessager();
        this.filer = environment.getFiler();
        types = environment.getTypeUtils();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        messager.printMessage(OTHER, "Start processor with " + annotations.size());

        try {
            Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Vertex.class);
            messager.printMessage(OTHER, elements.size() + " elements with annotation @Vertex");

            elements.stream().forEach(e -> generateImplementationClass((TypeElement) e, ElementType.Vertex));

            elements = roundEnv.getElementsAnnotatedWith(VertexProperty.class);
            messager.printMessage(OTHER, elements.size() + " elements with annotation @VertexProperty");
            elements.stream().filter(e -> e.getKind().isClass()).forEach(e -> generateVertexPropertyImplementationClass((TypeElement) e));

            elements = roundEnv.getElementsAnnotatedWith(Edge.class);
            messager.printMessage(OTHER, elements.size() + " elements with annotation @Edge");
            elements.stream().filter(e -> e.getKind().isClass() || e.getKind().isInterface()).forEach(e -> generateImplementationClass((TypeElement) e, ElementType.Edge));

            return true;
        } catch (Exception e) {
            try (StringWriter sw = new StringWriter(); PrintWriter out = new PrintWriter(sw)) {
                e.printStackTrace(out);
                messager.printMessage(ERROR, "Error while generating code: " + sw.toString());
                return false;
            } catch (IOException e1) {
                // ignore
                return false;
            }
        }
    }

    private void generateImplementationClass(TypeElement type, ElementType elementType) {
        TypeName implementsType = null;

        if (elementType == ElementType.Vertex) {
            implementsType = ParameterizedTypeName.get(ClassName.get(FramedVertex.class), ClassName.get(getBaseType(type)));

        } else if (elementType == ElementType.Edge) {
            implementsType = ClassName.get(FramedEdge.class);
        }

        ClassDescription description = parse(type);

        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        try {
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();

            String extendsType;
            Set<TypeName> implementsInterfaces = new HashSet<>();

            if (type.getKind().equals(INTERFACE)) {
                extendsType = null;

                Set<TypeElement> implementingInterfaces = getAllImplementingInterfaces(type); // add all interfaces extended by this interface
                implementingInterfaces.forEach(i -> implementsInterfaces.add(ClassName.bestGuess(i.getQualifiedName().toString())));
                implementsInterfaces.add(implementsType); // add the necessary interface
                implementsInterfaces.add(ClassName.get(type)); // add the interface itself
            } else { // it's a class or abstract class
                extendsType = type.getQualifiedName().toString();
                implementsInterfaces.add(implementsType);
            }

            MethodSpec constructor = MethodSpec.constructorBuilder().addModifiers(PUBLIC)
                    .addParameter(ClassName.get(elementType.getClazz()), elementType.getFieldName())
                    .addParameter(FramedGraph.class, "graph")
                    .addStatement("this.$L  = $L", elementType.getFieldName(), elementType.getFieldName())
                    .addStatement("this.graph = graph")
                    .build();

            MethodSpec graph = MethodSpec.methodBuilder("graph").addModifiers(PUBLIC).returns(peapod.FramedGraph.class)
                    .addStatement("return graph")
                    .build();

            MethodSpec element = MethodSpec.methodBuilder("element").addModifiers(PUBLIC).returns(org.apache.tinkerpop.gremlin.structure.Element.class)
                    .addStatement("return $L", elementType.getFieldName())
                    .build();

            TypeSpec.Builder implClass = TypeSpec.classBuilder(type.getSimpleName() + "$Impl")
                    .addModifiers(PUBLIC, FINAL);
            if (extendsType != null) {
                implClass.superclass(ClassName.bestGuess(extendsType));
            }
            implClass.addSuperinterfaces(implementsInterfaces)
                    .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build())
                    .addField(FramedGraph.class, "graph", PRIVATE)
                    .addField(elementType.getClazz(), elementType.getFieldName(), PRIVATE)
                    .addMethod(constructor)
                    .addMethod(graph)
                    .addMethod(element);


            implementAbstractMethods(description, implClass, elementType);
            implementFramerMethods(type, implClass, elementType, description.getPostConstructMethods());

            JavaFile javaFile = JavaFile.builder(packageEl.getQualifiedName().toString(), implClass.build()).build();
            javaFile.writeTo(filer);
        } catch (IOException e) {
            throw new RuntimeException("Error occurred while generating implementation for " + type.getQualifiedName(), e);
        }
    }

    private Set<TypeElement> getAllImplementingInterfaces(TypeElement type) {
        Set<TypeElement> results = new HashSet<>();
        for (TypeMirror tmp : type.getInterfaces()) {
            TypeElement typeElement = (TypeElement) ((DeclaredType) tmp).asElement();
            if (!typeElement.getQualifiedName().toString().startsWith("peapod.Framed")) {
                results.add(typeElement);
            }
        }
        return results;
    }

    private void generateVertexPropertyImplementationClass(TypeElement type) {
        ClassDescription description = parse(type);
        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        Optional<? extends TypeMirror> vertexPropertyClass = type.getInterfaces().stream().filter(i -> i.toString().contains("FramedVertexProperty")).findAny();
        if (!vertexPropertyClass.isPresent()) {
            messager.printMessage(ERROR, type.getQualifiedName() + " does not implement " + FramedVertexProperty.class);
            return;
        }

        DeclaredType vertexPropertyInterface = (DeclaredType) vertexPropertyClass.get();
        TypeMirror propertyType = vertexPropertyInterface.getTypeArguments().get(0);

        try {
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();


            TypeName vpType = ParameterizedTypeName.get(ClassName.get(org.apache.tinkerpop.gremlin.structure.VertexProperty.class), ClassName.get(propertyType));

            MethodSpec constructor = MethodSpec.constructorBuilder()
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(vpType, "vp")
                    .addParameter(FramedGraph.class, "graph")
                    .addStatement("this.$N = $N", "vp", "vp")
                    .addStatement("this.$N = $N", "graph", "graph")
                    .build();

            MethodSpec graph = MethodSpec.methodBuilder("graph")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(FramedGraph.class)
                    .addStatement("return $N", "graph")
                    .build();

            MethodSpec element = MethodSpec.methodBuilder("element")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(org.apache.tinkerpop.gremlin.structure.Element.class)
                    .addStatement("return $N", "vp")
                    .build();


            MethodSpec getValue = MethodSpec.methodBuilder("getValue")
                    .addModifiers(Modifier.PUBLIC)
                    .returns(ClassName.get(propertyType))
                    .addStatement("return vp.value()")
                    .build();

            String label = getLabel(type);
            MethodSpec setValue = MethodSpec.methodBuilder("setValue")
                    .addModifiers(Modifier.PUBLIC)
                    .addParameter(ClassName.get(propertyType), "value")
                    .beginControlFlow("if (value == null)")
                    .addStatement("vp.remove()")
                    .nextControlFlow("else")
                    .addStatement("vp.property($S, value)", label)
                    .endControlFlow()
                    .build();

            TypeSpec.Builder implClass = TypeSpec.classBuilder(type.getSimpleName() + "$Impl")
                    .superclass(ClassName.get(type))
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addAnnotation(AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build())
                    .addField(vpType, "vp", PRIVATE)
                    .addField(FramedGraph.class, "graph", PRIVATE)
                    .addMethod(constructor)
                    .addMethod(graph)
                    .addMethod(element)
                    .addMethod(getValue)
                    .addMethod(setValue);

            implementAbstractMethods(description, implClass, ElementType.VertexProperty);
            implementFramerMethods(type, implClass, ElementType.VertexProperty, description.getPostConstructMethods());

            JavaFile javaFile = JavaFile.builder(packageEl.getQualifiedName().toString(), implClass.build()).build();
            javaFile.writeTo(filer);

        } catch (IOException e) {
            messager.printMessage(WARNING, "An exception occurred while generating " + type.getQualifiedName() + "$Impl");
            throw new RuntimeException(e);
        }
    }

    private void implementAbstractMethods(ClassDescription description, TypeSpec.Builder implClass, ElementType elementType) throws IOException {
        for (ExecutableElement method : description.getMethods()) {
            MethodType methodType = MethodType.getType(method);
            MethodSpec m;
            if (description.isProperty(method)) {
                m = implementAbstractPropertyMethod(method, methodType, description.getLabel(method), elementType);
            } else {
                m = implementAbstractEdgeMethod(method, methodType, description.getLabel(method), elementType);
            }
            implClass.addMethod(m);
        }
    }

    private MethodSpec implementAbstractPropertyMethod(ExecutableElement method, MethodType methodType, String label, ElementType elementType) throws IOException {
        String fieldName = elementType.getFieldName();

        Set<Modifier> modifiers = new HashSet<>(method.getModifiers());
        modifiers.remove(ABSTRACT);

        MethodSpec.Builder builder1 = beginMethod(method, modifiers);

        Element parameterClass = method.getParameters().isEmpty() ? null : types.asElement(method.getParameters().get(0).asType());
        String parameterName = method.getParameters().isEmpty() ? null : method.getParameters().get(0).getSimpleName().toString();
        Element returnClass = method.getReturnType().getKind() == VOID ? null : types.asElement(method.getReturnType());


        if (methodType == MethodType.GETTER) {
            String className;
            if (method.getReturnType().getKind().isPrimitive()) {
                className = primitiveToClass(method.getReturnType());
            } else {
                className = types.asElement(method.getReturnType()).getSimpleName().toString();
            }

            CollectionType collectionType = getCollectionType(method.getReturnType());
            if (collectionType == null) {
                builder1.addStatement("return $L.<$L>property($S).orElse($L)", fieldName, className, label, getDefaultValue(method.getReturnType()));
            } else {
                TypeMirror singularizedType = getSingularizedType(method.getReturnType());

                if (isVertexProperty(singularizedType)) {
                    builder1.addStatement("return graph().frame(v.properties($S), $T.class)", label, singularizedType);
                } else {
                    builder1.addStatement("return $T.toList(v.values($S))", FrameHelper.class, label);
                }
            }
        } else if (methodType == MethodType.FILTERED_GETTER && isVertexProperty(method.getReturnType())) {
            builder1.addStatement("return $T.filterVertexProperty(this, $S, $L, $T.class)", FrameHelper.class, label, parameterName, method.getReturnType());
        } else if (methodType == MethodType.SETTER) {
            if (method.getParameters().get(0).asType().getKind().isPrimitive()) {
                builder1.addStatement(fieldName + ".$L($S, $L)", "property", label, parameterName);
            } else {
                builder1.beginControlFlow("if ($L == null)", parameterName)
                        .addStatement(fieldName + ".property($S).remove()", label)
                        .nextControlFlow("else")
                        .addStatement(fieldName + ".$L($S, $L)", "property", label, parameterName)
                        .endControlFlow();
            }
        } else if (methodType == MethodType.ADDER && parameterClass != null && returnClass == null) {
            builder1.addStatement("v.property(org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.list, $S, $L)", label, parameterName);
        } else if (methodType == MethodType.ADDER && parameterClass != null && isVertexProperty(method.getReturnType())) {
            builder1.addStatement("return graph.frame(v.property(org.apache.tinkerpop.gremlin.structure.VertexProperty.Cardinality.list, $S, $L), $T.class)", label, parameterName, method.getReturnType());
        } else if (methodType == MethodType.REMOVER && parameterClass != null && returnClass == null) {
            builder1.addStatement("$T.removeVertexProperty(this, $S, $L)", FrameHelper.class, label, parameterName);
        } else {
            generateNotSupportedStatement("nonstandard-property", method, builder1);
        }
        return builder1.build();
    }

    private MethodSpec implementAbstractEdgeMethod(ExecutableElement method, MethodType methodType, String label, ElementType elementType) throws IOException {
        String elementName = elementType.getFieldName();

        Set<Modifier> modifiers = new HashSet<>(method.getModifiers());
        modifiers.remove(ABSTRACT);

        MethodSpec.Builder m = beginMethod(method, modifiers);

        Element parameterClass = method.getParameters().isEmpty() ? null : types.asElement(method.getParameters().get(0).asType());
        String parameterName = method.getParameters().isEmpty() ? null : method.getParameters().get(0).getSimpleName().toString();
        Element returnClass = method.getReturnType().getKind() == VOID ? null : types.asElement(method.getReturnType());

        Direction direction = getDirection(method, methodType);

        if (methodType == MethodType.GETTER || methodType == MethodType.FILTERED_GETTER) {

            CollectionType collectionType = getCollectionType(method.getReturnType());
            if (collectionType != null) {
                DeclaredType returnType = (DeclaredType) method.getReturnType();
                if (returnType.getTypeArguments().size() != 1) {
                    messager.printMessage(ERROR, "Only one type argument supported: " + method);
                    return null;
                }

                TypeMirror collectionContent = returnType.getTypeArguments().get(0);

                Element element = types.asElement(collectionContent);
                Vertex vertexAnnotation = element.getAnnotation(Vertex.class);
                Edge edgeAnnotation = element.getAnnotation(Edge.class);

                if (vertexAnnotation != null) {
                    m.addCode("// getter-vertex-collection\n");
                    m.addStatement("return graph().frame($L.vertices($T.$L, $S), $T.class)", elementName, org.apache.tinkerpop.gremlin.structure.Direction.class, direction, label, collectionContent);
                } else if (edgeAnnotation != null) {
                    m.addCode("// getter-edge-collection\n");
                    m.addStatement("return graph.frame($L.edges($T.$L, $S), $T.class)", elementName, org.apache.tinkerpop.gremlin.structure.Direction.class, direction, label, collectionContent);
                } else {
                    generateNotSupportedStatement("get-collection-no-vertex-or-edge", method, m);
                }
            } else if (isVertex(method.getReturnType()) && elementType == ElementType.Vertex) {
                m.addCode("// vertex-getter-vertex\n");
                m.addStatement("$T<Vertex> it = v.vertices($T.$L, $S);", Iterator.class, org.apache.tinkerpop.gremlin.structure.Direction.class, direction, label)
                        .addStatement("return it.hasNext() ? graph.frame(it.next(), $T.class) : null", method.getReturnType());
            } else if (isVertex(method.getReturnType()) && elementType != ElementType.Vertex) {
                m.addCode("// edge-getter-vertex\n");
                boolean in = method.getAnnotation(In.class) != null;
                m.addStatement("return graph().frame($L.$LVertex(), $T.class)", elementName, in ? "in" : "out", method.getReturnType());
            } else if (isEdge(method.getReturnType()) && elementType == ElementType.Vertex) {
                m.addCode("// vertex-getter-edge\n");
                m.addStatement("return $T.filterEdge(this, $S, (($T)$L), $T.class)", FrameHelper.class, label, FramedVertex.class, parameterName, method.getReturnType());
            } else {
                generateNotSupportedStatement("get-no-vertex-or-edge", method, m);
            }
        } else if (methodType == MethodType.SETTER) {
            m.addCode("// vertex-setter-vertex\n");

            List<Object> args = new ArrayList<>();

            String statement = "v.addEdge($S, ((FramedVertex)$L).vertex())";
            args.add(label);
            args.add(parameterName);
            if (returnClass != null && returnClass.getAnnotation(Edge.class) != null) {
                statement = "return graph.frame(" + statement + ", $T.class)";
                args.add(method.getReturnType());
            }

            m.addStatement("v.edges($T.OUT, $S).forEachRemaining(e -> e.remove())", org.apache.tinkerpop.gremlin.structure.Direction.class, label)
                    .beginControlFlow("if ($L != null)", parameterName)
                    .addStatement(statement, args.toArray());
            if (method.getReturnType().getKind() != VOID) {
                m.nextControlFlow("else")
                        .addStatement("return null");
            }
            m.endControlFlow();
        } else if (methodType == MethodType.ADDER) {
            m.addCode("// vertex-adder-vertex\n");
            if (parameterClass != null && parameterClass.getAnnotation(Vertex.class) != null) {
                String statement = "v.addEdge($S, (($T) $L).vertex())";

                List<Object> args = new ArrayList<>();
                args.add(label);
                args.add(FramedVertex.class);
                args.add(parameterName);

                if (returnClass != null && returnClass.getAnnotation(Edge.class) != null) {
                    statement = "return graph.frame(" + statement + ", $T.class)";
                    args.add(method.getReturnType());
                }

                m.addStatement(statement, args.toArray());
            } else {
                generateNotSupportedStatement("added-without-vertex-parameter", method, m);
            }
        } else if (methodType == MethodType.REMOVER && parameterClass != null) {
            m.addCode("// vertex-remover-vertex\n");
            if (parameterClass.getAnnotation(Vertex.class) != null) {
                m.addStatement("$T.removeEdge(v, Direction.OUT, $S, (($T)$L).vertex())", FrameHelper.class, label, FramedVertex.class, parameterName);
            } else if (parameterClass.getAnnotation(Edge.class) != null) {
                m.addStatement("(($T)$L).remove()", FramedElement.class, parameterName);
            }
        }


        return m.build();
    }

    private void implementFramerMethods(TypeElement type, TypeSpec.Builder implClass, ElementType elementType, List<ExecutableElement> postContructMethods) throws IOException {
        String fieldName = elementType.getFieldName();

        MethodSpec hashCode = MethodSpec.methodBuilder("hashCode").addModifiers(PUBLIC).returns(TypeName.INT)
                .addStatement("return $L.hashCode()", fieldName)
                .build();
        implClass.addMethod(hashCode);

        MethodSpec equals = MethodSpec.methodBuilder("equals").addModifiers(PUBLIC).returns(TypeName.BOOLEAN)
                .addParameter(TypeName.OBJECT, "other")
                .addStatement("return (other instanceof $T) && $L.equals((($T) other).element())", FramedElement.class, fieldName, FramedElement.class)
                .build();
        implClass.addMethod(equals);

        MethodSpec toString = MethodSpec.methodBuilder("toString").addModifiers(PUBLIC).returns(String.class)
                .addStatement("return $L.label() + \"[\" + $L.id() + \"]\"", fieldName, fieldName)
                .build();
        implClass.addMethod(toString);

        ParameterizedTypeName framerInt = ParameterizedTypeName.get(ClassName.get(IFramer.class), TypeName.get(elementType.getClazz()), TypeName.get(type.asType()));


        MethodSpec mType = MethodSpec.methodBuilder("type").addModifiers(PUBLIC)
                .returns(ParameterizedTypeName.get(Class.class, elementType.getClazz()))
                .addStatement("return $L.class", elementType.getClazz().getSimpleName())
                .build();

        MethodSpec frameClass = MethodSpec.methodBuilder("frameClass").addModifiers(PUBLIC)
                .returns(ParameterizedTypeName.get(ClassName.get(Class.class), ClassName.get(type.asType())))
                .addStatement("return $T.class", type)
                .build();

        MethodSpec label = MethodSpec.methodBuilder("label").addModifiers(PUBLIC)
                .returns(String.class)
                .addStatement("return $S", getLabel(type))
                .build();

        MethodSpec frame = MethodSpec.methodBuilder("frame").addModifiers(PUBLIC)
                .returns(ClassName.get(type))
                .addParameter(elementType.getClazz(), fieldName)
                .addParameter(FramedGraph.class, "graph")
                .addStatement("return new $T$$Impl($L, graph)", type, fieldName)
                .build();


        MethodSpec.Builder frameNew = MethodSpec.methodBuilder("frameNew").addModifiers(PUBLIC)
                .returns(ClassName.get(type))
                .addParameter(elementType.getClazz(), fieldName)
                .addParameter(FramedGraph.class, "graph");
        if (postContructMethods.isEmpty()) {
            frameNew.addStatement("return frame($L, graph)", fieldName);
        } else {
            frameNew.addStatement("$L f = frame($L, graph)", type.getSimpleName(), fieldName);
            for (ExecutableElement m : postContructMethods) {
                frameNew.addStatement("f.$L()", m.getSimpleName());
            }
            frameNew.addStatement("return f");
        }

        TypeSpec framer = TypeSpec.classBuilder(type.getSimpleName() + "Framer").addModifiers(PUBLIC, STATIC, FINAL)
                .addSuperinterface(framerInt)
                .addAnnotation(Framer.class)
                .addMethod(mType)
                .addMethod(frameClass)
                .addMethod(label)
                .addMethod(frame)
                .addMethod(frameNew.build())
                .build();

        implClass.addType(framer);

    }

    private void generateNotSupportedStatement(String code, ExecutableElement method, MethodSpec.Builder writer) throws IOException {
        messager.printMessage(WARNING, "Abstract method not yet supported: " + method, method.getEnclosingElement());
        writer.addCode("// TODO: this method cannot be generated and should be implemented\n");
        writer.addStatement("throw new RuntimeException(\"" + code + ": not yet supported\")");
    }

    private TypeMirror getBaseType(TypeElement type) {
        TypeMirror superclass = type.getSuperclass();
        if (isVertex(superclass)) {
            return getBaseType((TypeElement) types.asElement(superclass));
        } else {
            return type.asType();
        }
    }

    private ClassDescription parse(TypeElement type) {

        Set<ExecutableElement> methods = new LinkedHashSet<>();
        List<ExecutableElement> postConstructs = new ArrayList<>();

        Stack<TypeElement> stack = new Stack<>();
        stack.push(type);

        TypeElement t;
        do {
            t = stack.pop();

            if (t.getQualifiedName().toString().matches("peapod.Framed(Element|Vertex|Edge|VertexProperty)")) {
                continue;
            }

            if (t.getKind().equals(INTERFACE)) {
                t.getEnclosedElements().stream().filter(e -> e.getKind() == METHOD && !e.getModifiers().contains(Modifier.DEFAULT)).forEach(e -> methods.add((ExecutableElement) e));
            } else {
                t.getEnclosedElements().stream().filter(e -> e.getKind() == METHOD && e.getModifiers().contains(Modifier.ABSTRACT)).forEach(e -> methods.add((ExecutableElement) e));
            }
            t.getEnclosedElements().stream().filter(e -> e.getKind() == METHOD && e.getAnnotation(PostConstruct.class) != null).forEach(e -> postConstructs.add((ExecutableElement) e));

            if (t.getSuperclass().getKind() == DECLARED) {
                t = (TypeElement) types.asElement(t.getSuperclass());
                stack.push(t);
            }
            t.getInterfaces().forEach(e -> {
                TypeElement typeElement = (TypeElement) ((DeclaredType) e).asElement();
                stack.push(typeElement);
            });
        }
        while (!stack.empty());

        // only retain unique signature methods. A duplicate method signature can come from inheriting multiple interfaces.
        Set<String> uniqueSignatures = new HashSet<>();
        List<ExecutableElement> toGenerate = methods.stream().filter(m -> uniqueSignatures.add(m.toString())).collect(Collectors.toList());

        ClassDescription description = new ClassDescription(type, toGenerate, postConstructs);

        Map<String, List<ExecutableElement>> property2Methods = methods.stream().collect(Collectors.groupingBy(this::extractProperty));
        property2Methods.forEach((p, l) -> parse(description, p, l));

        return description;
    }

    private void parse(ClassDescription description, String property, List<ExecutableElement> methods) {
        String label = null;

        boolean isProperty = true;

        for (ExecutableElement method : methods) {
            TypeMirror singularType;

            MethodType type = MethodType.getType(method);
            if (type == MethodType.SETTER || type == MethodType.REMOVER || (type == MethodType.ADDER && method.getReturnType().getKind() == VOID)) {
                singularType = getSingularizedType(method.getParameters().get(0).asType());
            } else {
                singularType = getSingularizedType(method.getReturnType());
            }

            CollectionType collectionType = getCollectionType(method.getReturnType());
            if (collectionType != null) {
                for (Class<?> clazz : collectionType.getImport()) {
                    description.addImport(clazz);
                }
            }

            boolean isVertex = isVertex(singularType);
            boolean isEdge = isEdge(singularType);

            if (isVertex || isEdge) {
                if ((description.getElementType() == ElementType.Edge) && type != MethodType.GETTER) {
                    messager.printMessage(ERROR, "@Edge classes cannot have vertex update methods: " + method);
                    continue;
                } else if ((description.getElementType() == ElementType.VertexProperty)) {
                    messager.printMessage(ERROR, "@VertexProperty classes can only have property methods: " + method);
                    continue;
                }

                if ((type == MethodType.GETTER || type == MethodType.FILTERED_GETTER) && collectionType == null && description.getElementType() == ElementType.Vertex) {
                    description.addImport(GraphTraversal.class);
                }

                if (type == MethodType.GETTER && isEdge) {
                    description.addImport(org.apache.tinkerpop.gremlin.structure.Edge.class);
                }

                if (isVertex) {
                    Edge edge = method.getAnnotation(Edge.class);
                    if (edge != null && !edge.value().isEmpty()) {
                        label = edge.value();
                    }
                } else {
                    TypeElement edgeClass = (TypeElement) types.asElement(singularType);
                    label = getLabel(edgeClass);
                }

                isProperty = false;
            } else {
                Property p = method.getAnnotation(Property.class);
                if (p != null && !p.value().isEmpty()) {
                    label = p.value();
                }

                if (type == MethodType.FILTERED_GETTER) {
                    description.addImport(GraphTraversal.class);
                    description.addImport(T.class);
                    description.addImport(org.apache.tinkerpop.gremlin.structure.VertexProperty.class);
                }
            }
        }

        if (label == null) {
            label = property;
        }

        String finalLabel = label;
        boolean finalIsProperty = isProperty;
        methods.forEach(m -> description.setLabel(m, finalLabel, finalIsProperty));

    }

    private String extractProperty(ExecutableElement method) {
        MethodType type = MethodType.getType(method);
        if (type == null) {
            messager.printMessage(WARNING, "Unsupported abstract method: " + method.getEnclosingElement() + "::" + method);
            return "";
        }

        String property = type.getPropertyName(method);
        if (getCollectionType(method.getReturnType()) != null) {
            property = Inflector.getInstance().singularize(property);
        }
        return property;
    }

    private TypeMirror getSingularizedType(TypeMirror type) {
        CollectionType collectionType = getCollectionType(type);
        if (collectionType == null) {
            return type;
        } else {
            DeclaredType returnType = (DeclaredType) type;
            if (returnType.getTypeArguments().size() != 1) {
                messager.printMessage(ERROR, "Only one type argument supported: " + type);
                throw new IllegalArgumentException("Only one type argument supported: " + type);
            }

            return returnType.getTypeArguments().get(0);
        }
    }

    private Direction getDirection(ExecutableElement method, MethodType type) {
        Direction direction;
        if (method.getAnnotation(In.class) != null) {
            direction = IN;
        } else if (method.getAnnotation(Both.class) != null) {
            direction = BOTH;
        } else {
            direction = OUT;
        }
        if (type != MethodType.GETTER && direction != OUT) {
            messager.printMessage(ERROR, "Direction " + direction + " only supported for getter methods currently");
            direction = OUT;
        }
        return direction;
    }

    private String getDefaultValue(TypeMirror type) {
        switch (type.getKind()) {
            case BOOLEAN:
                return "false";
            case BYTE:
                return "(byte) 0";
            case CHAR:
                return "'\u0000'";
            case DOUBLE:
                return "0.0d";
            case FLOAT:
                return "0.0f";
            case INT:
                return "0";
            case LONG:
                return "0L";
            case SHORT:
                return "(short) 0";
            default:
                return null;
        }
    }

    private String primitiveToClass(TypeMirror type) {
        switch (type.getKind()) {
            case BOOLEAN:
                return "Boolean";
            case BYTE:
                return "Byte";
            case CHAR:
                return "Character";
            case DOUBLE:
                return "Double";
            case FLOAT:
                return "Float";
            case INT:
                return "Integer";
            case LONG:
                return "Long";
            case SHORT:
                return "Short";
        }
        throw new IllegalArgumentException("Unrecognized primitive type: " + type.getKind());
    }

    private String getLabel(TypeElement type) {
        Vertex v = type.getAnnotation(Vertex.class);
        if (v != null) {
            return v.value().isEmpty() ? type.getSimpleName().toString() : v.value();
        }

        VertexProperty vp = type.getAnnotation(VertexProperty.class);
        if (vp != null) {
            return vp.value().isEmpty() ? type.getSimpleName().toString().toLowerCase() : vp.value();
        }

        Edge e = type.getAnnotation(Edge.class);
        if (e != null) {
            return e.value().isEmpty() ? type.getSimpleName().toString().toLowerCase() : e.value();
        }

        return null;
    }

    private CollectionType getCollectionType(TypeMirror type) {
        if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;

            boolean classOrInterface = declaredType.asElement().getKind().equals(CLASS) || declaredType.asElement().getKind().equals(INTERFACE);
            if (classOrInterface) {
                TypeElement element = (TypeElement) declaredType.asElement();
                try {
                    Class<?> clazz = Class.forName(element.getQualifiedName().toString());
                    boolean iterable = Iterable.class.isAssignableFrom(clazz);
                    if (iterable) {
                        if (List.class.equals(clazz)) {
                            return CollectionType.LIST;
                        } else if (Set.class.equals(clazz)) {
                            return CollectionType.SET;
                        } else if (Collection.class.equals(clazz)) {
                            return CollectionType.COLLECTION;
                        } else if (Iterable.class.equals(clazz)) {
                            return CollectionType.ITERABLE;
                        } else {
                            messager.printMessage(ERROR, "Unsupported Iterable<T> type: " + clazz);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    return null;
                }
            }
        }
        return null;
    }

    private enum CollectionType {
        LIST(List.class, Collections.class),
        COLLECTION(Collection.class, Collections.class),
        SET(Set.class, Collections.class),
        ITERABLE(DefaultIterable.class);

        private final Class<?>[] importClass;

        CollectionType(Class<?>... importClass) {
            this.importClass = importClass;
        }

        public Class<?>[] getImport() {
            return importClass;
        }
    }

    private boolean isVertex(TypeMirror type) {
        return type.getKind() == DECLARED && types.asElement(type).getAnnotation(Vertex.class) != null;
    }

    private boolean isVertexProperty(TypeMirror type) {
        return type.getKind() == DECLARED && types.asElement(type).getAnnotation(VertexProperty.class) != null;
    }

    private boolean isEdge(TypeMirror type) {
        return type.getKind() == DECLARED && types.asElement(type).getAnnotation(Edge.class) != null;
    }

    private enum MethodType {
        GETTER("get", 0),
        FILTERED_GETTER("get", 1),
        SETTER("set", 1),
        ADDER("add", 1),
        REMOVER("remove", 1);

        private final String prefix;
        private final int noParams;

        MethodType(String prefix, int noParams) {
            this.prefix = prefix;
            this.noParams = noParams;
        }

        private boolean isMethodType(ExecutableElement method) {
            return method.getSimpleName().toString().startsWith(prefix) && method.getParameters().size() == noParams;
        }

        public String getPropertyName(ExecutableElement method) {
            String property = method.getSimpleName().toString().substring(prefix.length());
            return property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
        }

        static MethodType getType(ExecutableElement method) {
            for (MethodType type : values()) {
                if (type.isMethodType(method)) {
                    return type;
                }
            }
            return null;
        }
    }

    private MethodSpec.Builder beginMethod(ExecutableElement method, Set<Modifier> modifiers) throws IOException {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(modifiers)
                .returns(ClassName.get(method.getReturnType()));

        for (VariableElement var : method.getParameters()) {
            builder.addParameter(ClassName.get(var.asType()), var.getSimpleName().toString());
        }

        return builder;
    }

}
