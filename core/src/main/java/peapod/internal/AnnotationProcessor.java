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

package peapod.internal;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import peapod.*;
import peapod.annotations.*;
import peapod.internal.runtime.DefaultIterable;
import peapod.internal.runtime.IFramer;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.PrintWriter;
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

    private final Map<TypeElement, List<String>> subTypes = new HashMap<>();

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
            elements.stream().forEach(e -> registerBaseClass((TypeElement) e));

            Class<?>[] vImports = {com.tinkerpop.gremlin.structure.Vertex.class, com.tinkerpop.gremlin.structure.Element.class, FramedVertex.class, FramedElement.class, FramedGraph.class, Collection.class, Arrays.class, Collections.class, IFramer.class};
            elements.stream().forEach(e -> generateImplementationClass((TypeElement) e, ElementType.Vertex, "FramedVertex<" + getBaseType((TypeElement) e).getSimpleName() + ">", vImports));

            elements = roundEnv.getElementsAnnotatedWith(VertexProperty.class);
            messager.printMessage(OTHER, elements.size() + " elements with annotation @VertexProperty");
            elements.stream().filter(e -> e.getKind().isClass()).forEach(e -> generateVertexPropertyImplementationClass((TypeElement) e));

            elements = roundEnv.getElementsAnnotatedWith(Edge.class);
            messager.printMessage(OTHER, elements.size() + " elements with annotation @Edge");
            Class<?>[] eImports = {com.tinkerpop.gremlin.structure.Edge.class, com.tinkerpop.gremlin.structure.Element.class, FramedEdge.class, FramedElement.class, FramedGraph.class, Collection.class, Arrays.class, Collections.class, IFramer.class};
            elements.stream().filter(e -> e.getKind().isClass()).forEach(e -> generateImplementationClass((TypeElement) e, ElementType.Edge, FramedEdge.class.getSimpleName(), eImports));

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private void registerBaseClass(TypeElement type) {
        String label = getLabel(type);

        TypeMirror superType = type.getSuperclass();
        while (superType.getKind() == DECLARED) {
            TypeElement superClass = (TypeElement) types.asElement(superType);

            List<String> labels = subTypes.get(superClass);
            if (labels == null) {
                labels = new ArrayList<>();
                subTypes.put(superClass, labels);
            }
            labels.add(label);

            superType = superClass.getSuperclass();
        }
    }

    private void generateImplementationClass(TypeElement type, ElementType elementType, String implementsType, Class<?>... imports) {
        ClassDescription description = parse(type);

        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "$Impl").openOutputStream())) {
            JavaWriterExt writer = new JavaWriterExt(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(imports)
                    .emitImports(description.getImports())
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "$Impl", "class", EnumSet.of(PUBLIC, Modifier.FINAL), type.getQualifiedName().toString(), implementsType)
                    .emitEmptyLine();
            String label = getLabel(type);
            writer.emitConstant("String", "LABEL", "\"" + label + "\"")
                    .emitEmptyLine()
                    .emitField(peapod.FramedGraph.class.getName(), "graph", EnumSet.of(PRIVATE))
                    .emitField(elementType.getClazz().getName(), elementType.getFieldName(), EnumSet.of(PRIVATE))
                    .beginConstructor(EnumSet.of(PUBLIC), elementType.toString(), elementType.getFieldName(), FramedGraph.class.getSimpleName(), "graph")
                    .emitStatement("this.%s  = %s", elementType.getFieldName(), elementType.getFieldName())
                    .emitStatement("this.graph = graph")
                    .endConstructor()
                    .beginMethod(peapod.FramedGraph.class.getSimpleName(), "graph", EnumSet.of(PUBLIC))
                    .emitStatement("return graph")
                    .endMethod()
                    .beginMethod("Element", "element", EnumSet.of(PUBLIC))
                    .emitStatement("return %s", elementType.getFieldName())
                    .endMethod();

            implementAbstractMethods(description, writer, elementType);
            implementFramerMethods(type, writer, elementType);

            writer.endType();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void generateVertexPropertyImplementationClass(TypeElement type) {
        ClassDescription description = parse(type);
        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        Optional<? extends TypeMirror> vertexPropertyClass = type.getInterfaces().stream().filter(i -> i.toString().contains("FramedVertexProperty")).findAny();
        if (!vertexPropertyClass.isPresent()) {
            messager.printMessage(ERROR, type.getQualifiedName() + " does not implement " + FramedVertexProperty.class);
        }

        DeclaredType vertexPropertyInterface = (DeclaredType) vertexPropertyClass.get();
        TypeMirror propertyType = vertexPropertyInterface.getTypeArguments().get(0);

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "$Impl").openOutputStream())) {
            JavaWriterExt writer = new JavaWriterExt(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(com.tinkerpop.gremlin.structure.VertexProperty.class, com.tinkerpop.gremlin.structure.Element.class, FramedElement.class, FramedGraph.class, FramedVertexProperty.class, Collection.class, Arrays.class, Collections.class, IFramer.class)
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "$Impl", "class", EnumSet.of(PUBLIC, Modifier.FINAL), type.getQualifiedName().toString()/*, FramedVertexProperty.class.getSimpleName()*/)
                    .emitEmptyLine();

            String label = getLabel(type);
            writer.emitConstant("String", "LABEL", "\"" + label + "\"")
                    .emitEmptyLine()
                    .emitField(peapod.FramedGraph.class.getSimpleName(), "graph", EnumSet.of(PRIVATE))
                    .emitField("VertexProperty<" + writer.compressType(propertyType) + ">", "vp", EnumSet.of(PRIVATE))
                    .beginConstructor(EnumSet.of(PUBLIC), "VertexProperty", "vp", FramedGraph.class.getSimpleName(), "graph")
                    .emitStatement("this.vp = vp")
                    .emitStatement("this.graph = graph")
                    .endConstructor()
                    .beginMethod(peapod.FramedGraph.class.getSimpleName(), "graph", EnumSet.of(PUBLIC))
                    .emitStatement("return graph")
                    .endMethod()
                    .beginMethod("Element", "element", EnumSet.of(PUBLIC))
                    .emitStatement("return vp")
                    .endMethod();


            HashSet<Modifier> modifiers = new HashSet<>();
            modifiers.add(PUBLIC);
            writer.beginMethod(propertyType.toString(), "getValue", modifiers)
                    .emitStatement("return vp.value()")
                    .endMethod();

            writer.beginMethod("void", "setValue", modifiers, writer.compressType(propertyType), "value")
                    .beginControlFlow("if (value == null)")
                    .emitStatement("vp.remove()")
                    .nextControlFlow("else")
                    .emitStatement("vp.property(\"%s\", value)", label)
                    .endControlFlow();
            writer.endMethod();

            implementAbstractMethods(description, writer, ElementType.VertexProperty);
            implementFramerMethods(type, writer, ElementType.VertexProperty);

            writer.endType();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void implementAbstractMethods(ClassDescription description, JavaWriterExt writer, ElementType elementType) throws IOException {
        for (ExecutableElement method : description.getMethods()) {
            MethodType methodType = MethodType.getType(method);
            if (description.isProperty(method)) {
                implementAbstractPropertyMethod(method, methodType, description.getLabel(method), writer, elementType);
            } else {
                implementAbstractEdgeMethod(method, methodType, description.getLabel(method), writer, elementType == ElementType.Vertex);
            }
        }
    }

    private void implementAbstractPropertyMethod(ExecutableElement method, MethodType methodType, String label, JavaWriterExt writer, ElementType elementType) throws IOException {
        String fieldName = elementType.getFieldName();
        boolean vertex = elementType == ElementType.Vertex;

        Set<Modifier> modifiers = new HashSet<>(method.getModifiers());
        modifiers.remove(ABSTRACT);
        writer.beginMethod(method, modifiers);

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
                writer.emitStatement("return %s.<%s>property(\"%s\").orElse(%s)", fieldName, className, label, getDefaultValue(method.getReturnType()));
            } else {
                TypeMirror singularizedType = getSingularizedType(method.getReturnType());

                if (isVertexProperty(singularizedType)) {
                    writer.emitStatement("return " + collectionType.wrap("v.properties(\"%s\").map(it -> (%s) new %s$Impl(it.get(), graph))"), label, writer.compressType(singularizedType), writer.compressType(singularizedType));
                } else {
                    writer.emitStatement("return " + collectionType.wrap("v.<%s>values(\"%s\")"), writer.compressType(singularizedType), label);
                }
            }
        } else if (methodType == MethodType.FILTERED_GETTER && isVertexProperty(method.getReturnType())) {
            writer.emitStatement("GraphTraversal<Vertex, %s> traversal = v.properties(\"%s\").<VertexProperty>has(T.value, %s).map(it -> (%s) new %s$Impl(it.get(), graph))", writer.compressType(method.getReturnType()), label, parameterName, writer.compressType(method.getReturnType()), writer.compressType(method.getReturnType()))
                    .emitStatement("return traversal.hasNext()? traversal.next() : null");
        } else if (methodType == MethodType.SETTER) {
            if (method.getParameters().get(0).asType().getKind().isPrimitive()) {
                writer.emitStatement(fieldName + ".%s(\"%s\", %s)", vertex ? "singleProperty" : "property", label, parameterName);
            } else {
                writer.beginControlFlow("if (%s == null)", parameterName)
                        .emitStatement(fieldName + ".property(\"%s\").remove()", label)
                        .nextControlFlow("else")
                        .emitStatement(fieldName + ".%s(\"%s\", %s)", vertex ? "singleProperty" : "property", label, parameterName)
                        .endControlFlow();
            }
        } else if (methodType == MethodType.ADDER && parameterClass != null && returnClass == null) {
            writer.emitStatement("v.property(\"%s\", %s)", label, parameterName);
        } else if (methodType == MethodType.ADDER && parameterClass != null && isVertexProperty(method.getReturnType())) {
            writer.emitStatement("return new %s$Impl(v.property(\"%s\", %s), graph)", writer.compressType(method.getReturnType()), label, parameterName);
        } else if (methodType == MethodType.REMOVER && parameterClass != null && returnClass == null) {
            writer.emitStatement("v.properties(\"%s\").has(com.tinkerpop.gremlin.process.T.value, %s).remove()", label, parameterName);
        } else {
            generateNotSupportedStatement("unsupported-property-method", method, writer);
        }
        writer.endMethod();
    }

    private void implementAbstractEdgeMethod(ExecutableElement method, MethodType methodType, String label, JavaWriterExt writer, boolean vertex) throws IOException {
        String elementName = vertex ? "v" : "e";

        Set<Modifier> modifiers = new HashSet<>(method.getModifiers());
        modifiers.remove(ABSTRACT);
        writer.beginMethod(method, modifiers);

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
                    return;
                }

                TypeMirror collectionContent = returnType.getTypeArguments().get(0);

                Element element = types.asElement(collectionContent);
                Vertex vertexAnnotation = element.getAnnotation(Vertex.class);
                Edge edgeAnnotation = element.getAnnotation(Edge.class);

                if (vertexAnnotation != null) {
                    String statement = String.format("%s.%s(\"%s\").map(v -> (%s) new %s$Impl(%s.get(), graph))", elementName, direction.toMethod(), label, element.toString(), element.toString(), elementName);
                    writer.emitStatement("return " + collectionType.wrap(statement));
                } else if (edgeAnnotation != null) {
                    String statement = String.format("%s.%sE(\"%s\").map(%s -> (%s) new %s$Impl(%s.get(), graph))", elementName, direction.toMethod(), label, elementName, element.toString(), element.toString(), elementName);

                    writer.emitStatement("return " + collectionType.wrap(statement));
                } else {
                    generateNotSupportedStatement("001", method, writer);
                }
            } else if (isVertex(method.getReturnType()) && vertex) {
                writer.emitStatement("%s<Vertex, %s> traversal = %s.%s(\"%s\").map(v -> new %s$Impl(v.get(), graph))",
                        writer.compressType(GraphTraversal.class), writer.compressType(method.getReturnType()), elementName, direction.toMethod(), label, returnClass)
                        .emitStatement("return traversal.hasNext()? traversal.next() : null");
            } else if (isEdge(method.getReturnType()) && vertex) {
                String filter = "";
                if (parameterName != null) {
                    filter = String.format(".as(\"X\").inV().retain(((FramedVertex) " + parameterName + ").vertex()).<%s>back(\"X\")", writer.compressType(com.tinkerpop.gremlin.structure.Edge.class));
                }

                writer.emitStatement("%s<Vertex, %s> traversal = %s.%sE(\"%s\")%s.map(v -> new %s$Impl(v.get(), graph))",
                        writer.compressType(GraphTraversal.class), writer.compressType(method.getReturnType()), elementName, direction.toMethod(), label, filter, returnClass)
                        .emitStatement("return traversal.hasNext()? traversal.next() : null");
            } else if (isVertex(method.getReturnType()) && !vertex) {
                boolean in = method.getAnnotation(In.class) != null;

                Element element = types.asElement(method.getReturnType());
                writer.emitStatement("return " + elementName + "." + (in ? "in" : "out") + "V().map(v -> new " + element.getSimpleName() + "$Impl(v.get(), graph)).next()");
            } else {
                generateNotSupportedStatement("003", method, writer);
            }
        } else if (methodType == MethodType.SETTER) {
            String statement = String.format("v.addEdge(\"%s\", ((FramedVertex)%s).vertex())", label, parameterName);
            if (returnClass != null && returnClass.getAnnotation(Edge.class) != null) {
                statement = String.format("return new %s$Impl(%s, graph)", returnClass.getSimpleName(), statement);
            }

            writer.emitStatement("v.outE(\"%s\").remove()", label)
                    .beginControlFlow("if (%s != null)", parameterName)
                    .emitStatement(statement);
            if (method.getReturnType().getKind() != VOID) {
                writer.nextControlFlow("else")
                        .emitStatement("return null");
            }
            writer.endControlFlow();
        } else if (methodType == MethodType.ADDER) {
            if (parameterClass != null && parameterClass.getAnnotation(Vertex.class) != null) {
                String statement = String.format("v.addEdge(\"%s\", ((FramedVertex) %s).vertex())", label, parameterName);
                if (returnClass != null && returnClass.getAnnotation(Edge.class) != null) {
                    statement = String.format("return new %s$Impl(%s, graph)", method.getReturnType().toString(), statement);
                }

                writer.emitStatement(statement);
            } else {
                generateNotSupportedStatement("002", method, writer);
            }
        } else if (methodType == MethodType.REMOVER && parameterClass != null) {
            if (parameterClass.getAnnotation(Vertex.class) != null) {
                writer.emitStatement("v.outE(\"%s\").as(\"X\").inV().retain(((FramedVertex)%s).vertex()).back(\"X\").remove()", label, parameterName);
            } else if (parameterClass.getAnnotation(Edge.class) != null) {
                writer.emitStatement("((FramedElement)%s).remove()", parameterName);
            }
        }

        writer.endMethod();
    }

    private void implementFramerMethods(TypeElement type, JavaWriterExt writer, ElementType elementType) throws IOException {
        String fieldName = elementType.getFieldName();

        writer.beginMethod("int", "hashCode", EnumSet.of(PUBLIC))
                .emitStatement("return %s.hashCode()", fieldName)
                .endMethod()
                .emitEmptyLine();
        writer.beginMethod("boolean", "equals", EnumSet.of(PUBLIC), Arrays.asList("Object", "other"), Collections.<String>emptyList())
                .emitStatement("return (other instanceof FramedElement) && %s.equals(((FramedElement) other).element())", fieldName)
                .endMethod()
                .emitEmptyLine();
        writer.beginMethod("String", "toString", EnumSet.of(PUBLIC))
                .emitStatement("return %s.label() + \"[\" + %s.id() + \"]\"", fieldName, fieldName)
                .endMethod()
                .emitEmptyLine();

        Set<Modifier> modifiers = new HashSet<>(Arrays.asList(Modifier.PRIVATE, Modifier.STATIC, FINAL));

        List<String> subLabels = subTypes.getOrDefault(type, Collections.emptyList());
        String initializer;
        if (subLabels.isEmpty()) {
            initializer = "Arrays.asList(LABEL)";
        } else {
            String collect = subLabels.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
            initializer = "Arrays.asList(LABEL, " + collect + ")";
        }

        writer.emitConstant("Framer", "instance", "new Framer()")
                .emitEmptyLine();
        String framer = "IFramer<" + elementType + ", " + type + ">";
        writer.beginType("Framer", "class", modifiers, null, framer)
                .emitEmptyLine()
                .emitField("Collection<String>", "subLabels", modifiers, "Collections.unmodifiableCollection(" + initializer + ")")
                .emitEmptyLine()
                .beginMethod("String", "label", Collections.singleton(PUBLIC))
                .emitStatement("return LABEL")
                .endMethod()
                .emitEmptyLine()
                .beginMethod("Collection<String>", "subLabels", Collections.singleton(PUBLIC))
                .emitStatement("return subLabels")
                .endMethod()
                .emitEmptyLine()
                .beginMethod(type.toString(), "frame", Collections.singleton(PUBLIC), elementType.toString(), fieldName, "FramedGraph", "graph")
                .emitStatement("return new %s$Impl(%s, graph)", type.getSimpleName(), fieldName)
                .endMethod()
                .endType();


    }

    private void generateNotSupportedStatement(String code, ExecutableElement method, JavaWriterExt writer) throws IOException {
        messager.printMessage(WARNING, "Abstract method not yet supported: " + method, method.getEnclosingElement());
        writer.emitStatement("throw new RuntimeException(\"" + code + ": not yet supported\")");
    }

    private TypeElement getBaseType(TypeElement type) {
        TypeMirror superclass = type.getSuperclass();
        if (isVertex(superclass)) {
            return getBaseType((TypeElement) types.asElement(superclass));
        } else {
            return type;
        }
    }

    private ClassDescription parse(TypeElement type) {
        List<ExecutableElement> elements = new ArrayList<>();

        TypeElement t = type;
        do {
            t.getEnclosedElements().stream().filter(e -> e.getKind() == METHOD && e.getModifiers().contains(Modifier.ABSTRACT)).forEach(e -> elements.add((ExecutableElement) e));
            if (t.getSuperclass().getKind() == DECLARED) {
                t = (TypeElement) types.asElement(t.getSuperclass());
            } else {
                t = null;
            }
        }
        while (t != null);

        ClassDescription description = new ClassDescription(type, elements);

        Map<String, List<ExecutableElement>> property2Methods = elements.stream().collect(Collectors.groupingBy(this::extractProperty));
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
                description.addImport(collectionType.getImport());
            }

            boolean isVertex = isVertex(singularType);
            boolean isEdge = isEdge(singularType);

            if (isVertex || isEdge) {
                if ((description.getElementType() == ElementType.Edge) && type != MethodType.GETTER) {
                    messager.printMessage(ERROR, "@Edge classes cannot have vertex update methods: " + method);
                    continue;
                } else if ((description.getElementType() == ElementType.VertexProperty)) {
                    messager.printMessage(ERROR, "@VertexProperty classes cannot only have property methods: " + method);
                    continue;
                }

                if (type == MethodType.GETTER && isVertex) {
                    description.addImport(GraphTraversal.class);
                    if (!method.getParameters().isEmpty()) {
                        description.addImport(com.tinkerpop.gremlin.structure.Edge.class);
                    }
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
                    description.addImport(com.tinkerpop.gremlin.structure.VertexProperty.class);
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
            messager.printMessage(ERROR, "Unsupported abstract method: " + method.getEnclosingElement() + "::" + method);
            return null;
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
        LIST("Collections.unmodifiableList(", ".toList())", List.class),
        COLLECTION("Collections.unmodifiableCollection(", ".toList())", Collection.class),
        SET("Collections.unmodifiableSet(", ".toSet())", Set.class),
        ITERABLE("new DefaultIterable(", ".iterate())", DefaultIterable.class);

        private final String prefix;
        private final String suffix;
        private final Class<? extends Iterable> importClass;

        CollectionType(String prefix, String suffix, Class<? extends Iterable> importClass) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.importClass = importClass;
        }

        public String wrap(String variable) {
            return prefix + variable + suffix;
        }

        public Class<? extends Iterable> getImport() {
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
            return prefix.equals(method.getSimpleName().subSequence(0, prefix.length())) && method.getParameters().size() == noParams;
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

}
