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

import com.tinkerpop.gremlin.process.Traversal;
import com.tinkerpop.gremlin.process.graph.GraphTraversal;
import peapod.*;
import peapod.annotations.Both;
import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Vertex;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.stream.Collectors;

import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.tools.Diagnostic.Kind.*;
import static javax.tools.Diagnostic.Kind.OTHER;
import static peapod.Direction.*;
import static peapod.impl.ClassDescription.ElementType.EDGE;

/**
 * Annotation processor for all {link @Vertex} annotated classes that generates the concrete implementation classes.
 */
@SupportedAnnotationTypes({"peapod.annotations.Vertex"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class AnnotationProcessor extends AbstractProcessor {

    private Messager messager;

    private Filer filer;

    private Types types;

    private Map<TypeElement, List<String>> subTypes = new HashMap<>();

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
            elements.stream().forEach(e -> generateVertexImplementationClass((TypeElement) e));

            elements = roundEnv.getElementsAnnotatedWith(Edge.class);
            messager.printMessage(OTHER, elements.size() + " elements with annotation @Edge");
            elements.stream().filter(e -> e.getKind().isClass()).forEach(e -> generateEdgeImplementationClass((TypeElement) e));

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

    private void generateVertexImplementationClass(TypeElement type) {
        ClassDescription description = parse(type);

        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "$Impl").openOutputStream())) {
            JavaWriter writer = new JavaWriter(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(com.tinkerpop.gremlin.structure.Vertex.class, com.tinkerpop.gremlin.structure.Element.class, FramedVertex.class, FramedElement.class, FramedGraph.class, Collection.class, Arrays.class, Collections.class)
                    .emitImports(description.getImports())
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "$Impl", "class", EnumSet.of(PUBLIC, Modifier.FINAL), type.getQualifiedName().toString(), "FramedVertex<" + getBaseType(type).getSimpleName() + ">")
                    .emitField(peapod.FramedGraph.class.getName(), "graph", EnumSet.of(PRIVATE))
                    .emitField(com.tinkerpop.gremlin.structure.Vertex.class.getName(), "v", EnumSet.of(PRIVATE))
                    .beginConstructor(EnumSet.of(PUBLIC), "Vertex", "v", FramedGraph.class.getSimpleName(), "graph")
                    .emitStatement("this.v = v")
                    .emitStatement("this.graph = graph")
                    .endConstructor()
                    .beginMethod(peapod.FramedGraph.class.getSimpleName(), "graph", EnumSet.of(PUBLIC))
                    .emitStatement("return graph")
                    .endMethod()
                    .beginMethod("Element", "element", EnumSet.of(PUBLIC))
                    .emitStatement("return v")
                    .endMethod();

            implementAbstractMethods(description, writer, true);
            implementFramerMethods(type, writer, true);

            writer.endType();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "Traversal").openOutputStream())) {
            JavaWriter writer = new JavaWriter(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(Traversal.class)
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "Traversal<S, E>", "interface", EnumSet.of(PUBLIC), "Traversal<S, E>")
                    .endType();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private TypeElement getBaseType(TypeElement type) {
        TypeMirror superclass = type.getSuperclass();
        if (hasAnnotation(superclass, Vertex.class)) {
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

            boolean isVertex = hasAnnotation(singularType, Vertex.class);
            boolean isEdge = hasAnnotation(singularType, Edge.class);

            if (isVertex || isEdge) {
                if (description.getElementType() == EDGE && type != MethodType.GETTER) {
                    messager.printMessage(ERROR, "@Edge classes cannot have linked vertex update methods: " + method);
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
                    if (edge != null && !edge.label().isEmpty()) {
                        label = edge.label();
                    }
                } else {
                    TypeElement edgeClass = (TypeElement) types.asElement(singularType);
                    label = getLabel(edgeClass);
                }

                isProperty = false;
            }
        }

        if (label == null) {
            label = property;
        }

        String finalLabel = label;
        boolean finalIsProperty = isProperty;
        methods.forEach(m -> description.setLabel(m, finalLabel, finalIsProperty));

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

    private void implementAbstractMethods(ClassDescription description, JavaWriter writer, boolean vertex) throws IOException {
        for (ExecutableElement method : description.getMethods()) {
            MethodType methodType = MethodType.getType(method);
            if (description.isProperty(method)) {
                implementAbstractPropertyMethod(method, methodType, description.getLabel(method), writer, vertex);
            } else {
                implementAbstractEdgeMethod(method, methodType, description.getLabel(method), writer, vertex);
            }
        }
    }

    private void implementAbstractPropertyMethod(ExecutableElement method, MethodType methodType, String label, JavaWriter writer, boolean vertex) throws IOException {
        String fieldName = vertex ? "v" : "e";

        Set<Modifier> modifiers = new HashSet<>(method.getModifiers());
        modifiers.remove(ABSTRACT);

        if (methodType == MethodType.GETTER) {
            String className;
            if (method.getReturnType().getKind().isPrimitive()) {
                className = primitiveToClass(method.getReturnType());
            } else {
                className = types.asElement(method.getReturnType()).getSimpleName().toString();
            }

            writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers)
                    .emitStatement("return %s.<%s>property(\"%s\").orElse(%s)", fieldName, className, label, getDefaultValue(method.getReturnType()))
                    .endMethod();

        } else if (methodType == MethodType.SETTER) {
            String parameterName = method.getParameters().isEmpty() ? null : method.getParameters().get(0).getSimpleName().toString();

            writer.beginMethod(method, modifiers);
            if (method.getParameters().get(0).asType().getKind().isPrimitive()) {
                writer.emitStatement(fieldName + ".%s(\"%s\", %s)", vertex ? "singleProperty" : "property", label, parameterName);
            } else {
                writer.beginControlFlow("if (%s == null)", parameterName)
                        .emitStatement(fieldName + ".property(\"%s\").remove()", label)
                        .nextControlFlow("else")
                        .emitStatement(fieldName + ".%s(\"%s\", %s)", vertex ? "singleProperty" : "property", label, parameterName)
                        .endControlFlow();
            }
            writer.endMethod();
        }
    }

    private void implementAbstractEdgeMethod(ExecutableElement method, MethodType methodType, String label, JavaWriter writer, boolean vertex) throws IOException {
        String fieldName = vertex ? "v" : "e";

        Set<Modifier> modifiers = new HashSet<>(method.getModifiers());
        modifiers.remove(ABSTRACT);

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
                    String statement = String.format("%s.%s(\"%s\").map(v -> (%s) new %s$Impl(%s.get(), graph)", fieldName, direction.toMethod(), label, element.toString(), element.toString(), fieldName);

                    writer.beginMethod(method, modifiers)
                            .emitStatement("return " + collectionType.wrap(statement))
                            .endMethod();
                } else if (edgeAnnotation != null) {
                    String statement = String.format("%s.%sE(\"%s\").map(%s -> (%s) new %s$Impl(%s.get(), graph)", fieldName, direction.toMethod(), label, fieldName, element.toString(), element.toString(), fieldName);

                    writer.beginMethod(method, modifiers)
                            .emitStatement("return " + collectionType.wrap(statement))
                            .endMethod();
                } else {
                    generateNotSupportedMethod("001", method, writer);
                }
            } else if (isVertex(method.getReturnType()) && vertex) {
                writer.beginMethod(method, modifiers)
                        .emitStatement("%s<Vertex, %s> traversal = %s.%s(\"%s\").map(v -> new %s$Impl(v.get(), graph))",
                                writer.compressType(GraphTraversal.class), writer.compressType(method.getReturnType()), fieldName, direction.toMethod(), label, returnClass)
                        .emitStatement("return traversal.hasNext()? traversal.next() : null")
                        .endMethod();
            } else if (isEdge(method.getReturnType()) && vertex) {
                String filter = "";
                if (parameterName != null) {
                    filter = String.format(".as(\"X\").inV().retain(((FramedVertex) " + parameterName + ").vertex()).<%s>back(\"X\")", writer.compressType(com.tinkerpop.gremlin.structure.Edge.class));
                }

                writer.beginMethod(method, modifiers)
                        .emitStatement("%s<Vertex, %s> traversal = %s.%sE(\"%s\")%s.map(v -> new %s$Impl(v.get(), graph))",
                                writer.compressType(GraphTraversal.class), writer.compressType(method.getReturnType()), fieldName, direction.toMethod(), label, filter, returnClass)
                        .emitStatement("return traversal.hasNext()? traversal.next() : null")
                        .endMethod();
            } else if (isVertex(method.getReturnType()) && !vertex) {
                boolean in = method.getAnnotation(In.class) != null;

                Element element = types.asElement(method.getReturnType());
                writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers)
                        .emitStatement("return " + fieldName + "." + (in ? "in" : "out") + "V().map(v -> new " + element.getSimpleName() + "$Impl(v.get(), graph)).next()")
                        .endMethod();
            } else {
                generateNotSupportedMethod("003", method, writer);
            }
        } else if (methodType == MethodType.SETTER) {
            String statement = String.format("v.addEdge(\"%s\", ((FramedVertex)%s).vertex())", label, label);
            if (returnClass != null && returnClass.getAnnotation(Edge.class) != null) {
                statement = String.format("return new %s$Impl(%s, graph)", returnClass.getSimpleName(), statement);
            }

            writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers, method.getParameters().get(0).asType().toString(), label)
                    .emitStatement("v.outE(\"%s\").remove()", label)
                    .beginControlFlow("if (%s != null)", label)
                    .emitStatement(statement);
            if (method.getReturnType().getKind() != VOID) {
                writer.nextControlFlow("else")
                        .emitStatement("return null");
            }
            writer.endControlFlow();
            writer.endMethod();

        } else if (methodType == MethodType.ADDER) {
            if (parameterClass != null && parameterClass.getAnnotation(Vertex.class) != null) {
                String statement = String.format("v.addEdge(\"%s\", ((FramedVertex) %s).vertex())", label, parameterName);
                if (returnClass != null && returnClass.getAnnotation(Edge.class) != null) {
                    statement = String.format("return new %s$Impl(%s, graph)", method.getReturnType().toString(), statement);
                }

                writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers, parameterClass.toString(), parameterName);
                writer.emitStatement(statement);
                writer.endMethod();
            } else {
                generateNotSupportedMethod("002", method, writer);
            }
        } else if (methodType == MethodType.REMOVER && parameterClass != null) {
            writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers, parameterClass.toString(), parameterName);
            if (parameterClass.getAnnotation(Vertex.class) != null) {
                writer.emitStatement("v.outE(\"%s\").as(\"X\").inV().retain(((FramedVertex)%s).vertex()).back(\"X\").remove()", label, parameterName);
            } else if (parameterClass.getAnnotation(Edge.class) != null) {
                writer.emitStatement("((FramedElement)%s).remove()", parameterName);
            }
            writer.endMethod();
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

    private boolean hasAnnotation(TypeMirror type, Class<? extends Annotation> annotation) {
        if (type.getKind() != DECLARED) {
            return false;
        }
        Element element = types.asElement(type);
        return element.getAnnotation(annotation) != null;
    }

    private void generateNotSupportedMethod(String code, ExecutableElement method, JavaWriter writer) throws IOException {
        messager.printMessage(WARNING, "Abstract method not yet supported: " + method, method.getEnclosingElement());

        List<String> parameters = new ArrayList<>();
        for (VariableElement var : method.getParameters()) {
            parameters.add(var.asType().toString());
            parameters.add(var.toString());
        }

        Set<Modifier> modifiers = new HashSet<>(method.getModifiers());
        modifiers.remove(ABSTRACT);

        writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers, parameters, null)
                .emitStatement("throw new RuntimeException(\"" + code + ": not yet supported\")")
                .endMethod();
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

    private void generateEdgeImplementationClass(TypeElement type) {
        ClassDescription description = parse(type);
        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "$Impl").openOutputStream())) {
            JavaWriter writer = new JavaWriter(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(com.tinkerpop.gremlin.structure.Edge.class, com.tinkerpop.gremlin.structure.Element.class, FramedEdge.class, FramedElement.class, FramedGraph.class, Collection.class, Arrays.class, Collections.class)
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "$Impl", "class", EnumSet.of(PUBLIC, Modifier.FINAL), type.getQualifiedName().toString(), FramedEdge.class.getSimpleName())
                    .emitField(peapod.FramedGraph.class.getSimpleName(), "graph", EnumSet.of(PRIVATE))
                    .emitField(com.tinkerpop.gremlin.structure.Edge.class.getName(), "e", EnumSet.of(PRIVATE))
                    .beginConstructor(EnumSet.of(PUBLIC), "Edge", "e", FramedGraph.class.getSimpleName(), "graph")
                    .emitStatement("this.e = e")
                    .emitStatement("this.graph = graph")
                    .endConstructor()
                    .beginMethod(peapod.FramedGraph.class.getSimpleName(), "graph", EnumSet.of(PUBLIC))
                    .emitStatement("return graph")
                    .endMethod()
                    .beginMethod("Element", "element", EnumSet.of(PUBLIC))
                    .emitStatement("return e")
                    .endMethod();

            implementAbstractMethods(description, writer, false);
            implementFramerMethods(type, writer, false);

            writer.endType();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void implementFramerMethods(TypeElement type, JavaWriter writer, boolean vertex) throws IOException {
        String fieldName = vertex ? "v" : "e";

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

        String label = getLabel(type);

        List<String> subLabels = subTypes.getOrDefault(type, Collections.emptyList());
        String initializer;
        if (subLabels.isEmpty()) {
            initializer = "Arrays.asList(label)";
        } else {
            String collect = subLabels.stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(","));
            initializer = "Arrays.asList(label, " + collect + ")";
        }

        String framer = "peapod.Framer<" + (vertex ? "Vertex" : "Edge") + ", " + type + ">";
        writer.beginType("Framer", "class", modifiers, null, framer)
                .emitEmptyLine()
                .emitField("Framer", "instance", modifiers, "new Framer()")
                .emitField("String", "label", modifiers, "\"" + label + "\"")
                .emitField("Collection<String>", "subLabels", modifiers, "Collections.unmodifiableCollection(" + initializer + ")")
                .emitEmptyLine()
                .beginMethod("String", "label", Collections.singleton(PUBLIC))
                .emitStatement("return label")
                .endMethod()
                .emitEmptyLine()
                .beginMethod("Collection<String>", "subLabels", Collections.singleton(PUBLIC))
                .emitStatement("return subLabels")
                .endMethod()
                .emitEmptyLine()
                .beginMethod(type.toString(), "frame", Collections.singleton(PUBLIC), vertex ? "Vertex" : "Edge", fieldName, "FramedGraph", "graph")
                .emitStatement("return new %s$Impl(%s, graph)", type.getSimpleName(), fieldName)
                .endMethod()
                .endType()
                .emitEmptyLine();

        modifiers = new HashSet<>(Arrays.asList(Modifier.PUBLIC, Modifier.STATIC));
        writer.beginMethod(framer, "framer", modifiers)
                .emitStatement("return Framer.instance")
                .endMethod();

    }

    private String getLabel(TypeElement type) {
        Vertex v = type.getAnnotation(Vertex.class);
        if (v != null) {
            return v.label().isEmpty() ? type.getSimpleName().toString() : v.label();
        }

        Edge e = type.getAnnotation(Edge.class);
        if (e != null) {
            return e.label().isEmpty() ? type.getSimpleName().toString().toLowerCase() : e.label();
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

    private boolean isVertex(TypeMirror type) {
        if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            return hasAnnotation(declaredType, Vertex.class);
        }
        return false;
    }

    private boolean isEdge(TypeMirror type) {
        if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;
            return hasAnnotation(declaredType, Edge.class);
        }
        return false;
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

    private enum CollectionType {
        LIST("Collections.unmodifiableList(", ").toList())", null),
        COLLECTION("Collections.unmodifiableCollection(", ").toList())", null),
        SET("Collections.unmodifiableSet(", ").toSet())", null),
        ITERABLE("new DefaultIterable(", ").iterate())", "peapod.impl.DefaultIterable");

        private final String prefix;
        private final String suffix;
        private String importClass;

        CollectionType(String prefix, String suffix, String importClass) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.importClass = importClass;
        }

        public String wrap(String variable) {
            return prefix + variable + suffix;
        }

        public String getImport() {
            return importClass;
        }
    }
}
