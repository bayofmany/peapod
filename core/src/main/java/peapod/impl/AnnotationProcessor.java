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

import com.google.common.base.Preconditions;
import com.squareup.javawriter.JavaWriter;
import com.tinkerpop.gremlin.process.Traversal;
import peapod.Direction;
import peapod.FramedEdge;
import peapod.FramedGraph;
import peapod.FramedVertex;
import peapod.annotations.*;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.util.*;

import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.type.TypeKind.DECLARED;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.tools.Diagnostic.Kind.*;
import static javax.tools.Diagnostic.Kind.OTHER;
import static peapod.Direction.OUT;

/**
 * Annotation processor for all {link @Vertex} annotated classes that generates the concrete implementation classes.
 * Created by Willem on 26/12/2014.
 */
@SupportedAnnotationTypes({"peapod.annotations.Vertex"})
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

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Vertex.class);
        messager.printMessage(OTHER, elements.size() + " elements with annotation @Vertex");
        elements.stream().forEach(e -> generateVertexImplementationClass((TypeElement) e));

        elements = roundEnv.getElementsAnnotatedWith(Edge.class);
        messager.printMessage(OTHER, elements.size() + " elements with annotation @Edge");
        elements.stream().forEach(e -> generateEdgeImplementationClass((TypeElement) e));

        return true;
    }

    private void generateVertexImplementationClass(TypeElement type) {
        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "$Impl").openOutputStream())) {
            JavaWriter writer = new JavaWriter(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(com.tinkerpop.gremlin.structure.Vertex.class, com.tinkerpop.gremlin.structure.Element.class, FramedVertex.class, FramedGraph.class)
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "$Impl", "class", EnumSet.of(PUBLIC, Modifier.FINAL), type.getQualifiedName().toString(), FramedVertex.class.getName())
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
                    .endMethod()
                    .beginMethod("Vertex", "vertex", EnumSet.of(PUBLIC))
                    .emitStatement("return v")
                    .endMethod();

            implementAbstractMethods(type, "v", writer, true);

            writer.beginMethod("int", "hashCode", EnumSet.of(PUBLIC))
                    .emitStatement("return v.hashCode()")
                    .endMethod();
            writer.beginMethod("boolean", "equals", EnumSet.of(PUBLIC), Arrays.asList("Object", "other"), Collections.<String>emptyList())
                    .emitStatement("return (other instanceof FramedVertex) ? v.equals(((FramedVertex) other).vertex()) : false")
                    .endMethod();
            writer.beginMethod("String", "toString", EnumSet.of(PUBLIC))
                    .emitStatement("return v.label() + \"[\" + v.id() + \"]\"")
                    .endMethod();

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

    private void implementAbstractMethods(TypeElement type, String fieldName, JavaWriter writer, boolean vertex) throws IOException {
        List<Element> elements = new ArrayList<>(type.getEnclosedElements());

        TypeMirror superType = type.getSuperclass();
        while (superType.getKind() == TypeKind.DECLARED) {
            TypeElement element = (TypeElement) types.asElement(superType);
            elements.addAll(element.getEnclosedElements());
            superType = element.getSuperclass();
        }

        for (Element el : elements) {
            if (el.getKind() != METHOD) {
                continue;
            }

            ExecutableElement method = (ExecutableElement) el;
            if (!method.getModifiers().contains(ABSTRACT)) {
                continue;
            }

            Set<Modifier> modifiers = new HashSet<>(el.getModifiers());
            modifiers.remove(ABSTRACT);

            if (isGetter(method)) {
                boolean hidden = isHiddenProperty(method);
                String property = getPropertyName(method, "get");

                CollectionType collectionType = getCollectionType(method.getReturnType());
                if (collectionType != null) {
                    DeclaredType returnType = (DeclaredType) method.getReturnType();
                    Preconditions.checkState(returnType.getTypeArguments().size() == 1, "Only one type argument supported");
                    TypeMirror collectionContent = returnType.getTypeArguments().get(0);

                    Element element = types.asElement(collectionContent);
                    Vertex vertexAnnotation = element.getAnnotation(Vertex.class);
                    Edge edgeAnnotation = element.getAnnotation(Edge.class);

                    if (vertexAnnotation != null) {
                        LinkedVertex linked = method.getAnnotation(LinkedVertex.class);
                        String edgeLabel = linked == null ? NounHelper.isPlural(property) ? NounHelper.singularize(property) : property : linked.label();

                        Direction direction = linked == null ? OUT : linked.direction();
                        String statement = String.format("%s.%s(\"%s\").map(v -> (%s) new %s$Impl(%s.get(), graph)", fieldName, direction.toMethod(), edgeLabel, element, element, fieldName);

                        writer.beginMethod(returnType.toString(), method.getSimpleName().toString(), modifiers)
                                .emitStatement("return " + collectionType.wrap(statement))
                                .endMethod();
                    } else if (edgeAnnotation != null) {
                        LinkedEdge linked = method.getAnnotation(LinkedEdge.class);
                        Direction direction = linked == null ? OUT : linked.direction();

                        String edgeLabel = edgeAnnotation.label();
                        if ("edge".equals(edgeLabel)) {
                            edgeLabel = element.getSimpleName().toString().toLowerCase();
                        }

                        String statement = String.format("%s.%sE(\"%s\").map(%s -> (%s) new %s$Impl(%s.get(), graph)", fieldName, direction.toMethod(), edgeLabel, fieldName, element, element, fieldName);

                        writer.beginMethod(returnType.toString(), method.getSimpleName().toString(), modifiers)
                                .emitStatement("return " + collectionType.wrap(statement))
                                .endMethod();
                    } else {
                        generateNotSupportedMethod("001", method, writer);
                    }
                } else if (isVertex(method.getReturnType()) && vertex) {
                    String edgeLabel = getEdgeLabel(method, property);

                    Direction direction = method.getAnnotation(LinkedVertex.class) == null ? OUT : method.getAnnotation(LinkedVertex.class).direction();

                    Element element = types.asElement(method.getReturnType());
                    writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers)
                            .emitStatement("com.tinkerpop.gremlin.process.graph.GraphTraversal<Vertex, %s> traversal = %s.%s(\"%s\").map(v -> new %s$Impl(v.get(), graph))",
                                    method.getReturnType().toString(), fieldName, direction.toMethod(), edgeLabel, element.getSimpleName())
                            .emitStatement("return traversal.hasNext()? traversal.next() : null")
                            .endMethod();
                } else if (isVertex(method.getReturnType()) && !vertex) {
                    boolean in = method.getAnnotation(In.class) != null;

                    Element element = types.asElement(method.getReturnType());
                    writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers)
                            .emitStatement("return " + fieldName + "." + (in ? "in" : "out") + "V().map(v -> new " + element.getSimpleName() + "$Impl(v.get(), graph)).next()")
                            .endMethod();
                } else {
                    String className;
                    if (method.getReturnType().getKind().isPrimitive()) {
                        className = primitiveToClass(method.getReturnType());
                    } else {
                        className = method.getReturnType().toString();
                    }

                    String propertyName = "\"" + property + "\"";
                    propertyName = hidden ? "com.tinkerpop.gremlin.structure.Graph.Key.hide(" + propertyName + ")" : propertyName;

                    writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers)
                            .emitStatement("return %s.<%s>property(%s).orElse(%s)", fieldName, className, propertyName, getDefaultValue(method.getReturnType()))
                            .endMethod();
                }
            } else if (isSetter(method)) {
                String property = getPropertyName(method, "set");
                String edgeLabel = getEdgeLabel(method, property);

                TypeMirror propertyType = method.getParameters().get(0).asType();
                Element propertyTypeElement = types.asElement(propertyType);

                Vertex vertexAnnotation = propertyTypeElement == null ? null : propertyTypeElement.getAnnotation(Vertex.class);
                Edge edgeAnnotation = propertyTypeElement == null ? null : propertyTypeElement.getAnnotation(Edge.class);
                if (vertexAnnotation != null) {
                    writer.beginMethod("void", method.getSimpleName().toString(), modifiers, propertyTypeElement.toString(), property)
                            .emitStatement("v.outE(\"%s\").remove()", edgeLabel)
                            .beginControlFlow("if (" + property + " != null)")
                            .emitStatement("v.addEdge(\"%s\", ((FramedVertex)%s).vertex())", edgeLabel, property)
                            .endControlFlow();
                    writer.endMethod();
                } else if (edgeAnnotation != null) {
                    generateNotSupportedMethod("004", method, writer);

                } else {
                    String propertyName = "\"" + property + "\"";
                    boolean hidden = isHiddenProperty(method);
                    propertyName = hidden ? "com.tinkerpop.gremlin.structure.Graph.Key.hide(" + propertyName + ")" : propertyName;

                    writer.beginMethod("void", method.getSimpleName().toString(), modifiers, propertyType.toString(), property);
                    boolean isPrimitive = propertyType.getKind().isPrimitive();
                    if (isPrimitive) {
                        writer.emitStatement(fieldName + ".%s(%s, %s)", vertex ? "singleProperty" : "property", propertyName, property);
                    } else {
                        writer.beginControlFlow("if (" + property + " == null)")
                                .emitStatement(fieldName + ".property(%s).remove()", propertyName)
                                .nextControlFlow("else")
                                .emitStatement(fieldName + ".%s(%s, %s)", vertex ? "singleProperty" : "property", propertyName, property)
                                .endControlFlow();
                    }
                    writer.endMethod();
                }
            } else if (isAdder(method)) {
                String property = getPropertyName(method, "add");
                String edgeLabel = getEdgeLabel(method, property);

                VariableElement parameter = method.getParameters().get(0);
                Element propertyType = types.asElement(parameter.asType());

                if (hasAnnotation(parameter.asType(), Vertex.class)) {
                    String statement = String.format("v.addEdge(\"%s\", ((FramedVertex) %s).vertex())", edgeLabel, parameter.getSimpleName().toString());
                    if (hasAnnotation(method.getReturnType(), Edge.class)) {
                        statement = String.format("return new %s$Impl(%s, graph)", method.getReturnType().toString(), statement);
                    } else if (method.getReturnType().getKind() != VOID) {
                        generateNotSupportedMethod("005", method, writer);
                        continue;
                    }

                    writer.beginMethod(method.getReturnType().toString(), method.getSimpleName().toString(), modifiers, propertyType.toString(), parameter.getSimpleName().toString());
                    writer.emitStatement(statement);
                    writer.endMethod();
                } else {
                    generateNotSupportedMethod("002", method, writer);
                }
            } else {
                //                            .emitStatement("v.outE(\"%s\").as(\"X\").inV().retain(((FramedVertex)%s).vertex()).back(\"X\").remove()",
//                                    property, property)

                generateNotSupportedMethod("003", method, writer);
            }

        }
    }

    private String getEdgeLabel(ExecutableElement method, String property) {
        String edgeLabel = property;
        LinkedVertex linkedVertexAnnotation = method.getAnnotation(LinkedVertex.class);
        if (linkedVertexAnnotation != null && linkedVertexAnnotation.label() != null) {
            edgeLabel = linkedVertexAnnotation.label();
        }
        return edgeLabel;
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

    private boolean isHiddenProperty(ExecutableElement executableEl) {
        Property annotation = executableEl.getAnnotation(Property.class);
        boolean hidden = false;
        if (annotation != null) {
            hidden = annotation.hidden();
        }
        return hidden;
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
        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "$Impl").openOutputStream())) {
            JavaWriter writer = new JavaWriter(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(com.tinkerpop.gremlin.structure.Edge.class, com.tinkerpop.gremlin.structure.Element.class, FramedEdge.class, FramedGraph.class)
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "$Impl", "class", EnumSet.of(PUBLIC, Modifier.FINAL), type.getQualifiedName().toString())
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
                    .endMethod()
                    .beginMethod("Edge", "edge", EnumSet.of(PUBLIC))
                    .emitStatement("return e")
                    .endMethod();

            implementAbstractMethods(type, "e", writer, false);

            writer.beginMethod("int", "hashCode", EnumSet.of(PUBLIC))
                    .emitStatement("return e.hashCode()")
                    .endMethod();
            writer.beginMethod("boolean", "equals", EnumSet.of(PUBLIC), Arrays.asList("Object", "other"), Collections.<String>emptyList())
                    .emitStatement("return (other instanceof FramedEdge) ? e.equals(((FramedEdge) other).edge()) : false")
                    .endMethod();
            writer.beginMethod("String", "toString", EnumSet.of(PUBLIC))
                    .emitStatement("return e.label() + \"[\" + e.id() + \"]\"")
                    .endMethod();

            writer.endType();
        } catch (IOException e) {
            e.printStackTrace();
        }

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
            return types.asElement(declaredType).getAnnotation(Vertex.class) != null;
        }
        return false;
    }


    private boolean isGetter(ExecutableElement el) {
        return "get".equals(el.getSimpleName().subSequence(0, 3)) && el.getParameters().isEmpty();
    }

    private boolean isSetter(ExecutableElement el) {
        return "set".equals(el.getSimpleName().subSequence(0, 3)) && el.getParameters().size() == 1 && el.getReturnType().getKind() == VOID;
    }

    private boolean isAdder(ExecutableElement el) {
        return "add".equals(el.getSimpleName().subSequence(0, 3)) && el.getParameters().size() == 1;
    }

    private String getPropertyName(ExecutableElement method, String prefix) {
        String property = method.getSimpleName().toString().substring(prefix.length());
        return property.substring(0, 1).toLowerCase() + property.substring(1, property.length());
    }

    private enum CollectionType {
        LIST("java.util.Collections.unmodifiableList(", ").toList())"),
        COLLECTION("java.util.Collections.unmodifiableCollection(", ").toList())"),
        SET("java.util.Collections.unmodifiableSet(", ").toSet())"),
        ITERABLE("new peapod.impl.DefaultIterable(", ").iterate())");

        private final String prefix;
        private final String suffix;

        CollectionType(String prefix, String suffix) {
            this.prefix = prefix;
            this.suffix = suffix;
        }

        public String wrap(String variable) {
            return prefix + variable + suffix;
        }
    }
}
