package peapod.impl;

import com.google.common.base.Preconditions;
import com.squareup.javawriter.JavaWriter;
import com.tinkerpop.gremlin.structure.VertexProperty;
import peapod.Direction;
import peapod.FramedEdge;
import peapod.FramedVertex;
import peapod.annotations.*;
import org.slf4j.Logger;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static javax.lang.model.element.ElementKind.*;
import static javax.lang.model.element.Modifier.*;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.Diagnostic.Kind.OTHER;
import static org.slf4j.LoggerFactory.getLogger;
import static peapod.Direction.OUT;

/**
 * Annotation processor for all {link @Vertex} annotated classes that generates the concrete implementation classes.
 * Created by Willem on 26/12/2014.
 */
@SupportedAnnotationTypes({"peapod.annotations.Vertex"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public final class AnnotationProcessor extends AbstractProcessor {

    private static final Logger log = getLogger(AnnotationProcessor.class);

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

    public void generateVertexImplementationClass(TypeElement type) {
        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "$Impl").openOutputStream())) {
            JavaWriter writer = new JavaWriter(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(com.tinkerpop.gremlin.structure.Vertex.class, VertexProperty.class, FramedVertex.class)
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "$Impl", "class", EnumSet.of(PUBLIC, Modifier.FINAL), type.getQualifiedName().toString(), FramedVertex.class.getName())
                    .emitField(com.tinkerpop.gremlin.structure.Vertex.class.getName(), "v", EnumSet.of(PRIVATE))
                    .beginConstructor(EnumSet.of(PUBLIC), "Vertex", "v")
                    .emitStatement("this.v = v")
                    .endConstructor()
                    .beginMethod(com.tinkerpop.gremlin.structure.Vertex.class.getName(), "vertex", EnumSet.of(PUBLIC))
                    .emitStatement("return v")
                    .endMethod();

            implementAbstractMethods(type, "v", writer);

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

    }

    private void implementAbstractMethods(TypeElement type, String fieldName, JavaWriter writer) throws IOException {
        for (Element el : type.getEnclosedElements()) {
            if (el.getKind() == METHOD) {
                ExecutableElement executableEl = (ExecutableElement) el;
                if (isGetter(executableEl)) {
                    boolean hidden = isHiddenProperty(executableEl);
                    String property = getPropertyName(executableEl);

                    Set<Modifier> modifiers = new HashSet<>(el.getModifiers());
                    modifiers.remove(ABSTRACT);

                    CollectionType collectionType = getCollectionType(executableEl.getReturnType());
                    if (collectionType != null) {
                        DeclaredType returnType = (DeclaredType) executableEl.getReturnType();
                        Preconditions.checkState(returnType.getTypeArguments().size() == 1, "Only one type argument supported");
                        TypeMirror collectionContent = returnType.getTypeArguments().get(0);
                        log.info("Collection parameter type is {}", collectionContent);

                        Element element = types.asElement(collectionContent);
                        Vertex vertexAnnotation = element.getAnnotation(Vertex.class);
                        Edge edgeAnnotation = element.getAnnotation(Edge.class);

                        if (vertexAnnotation != null) {
                            LinkedVertex linked = executableEl.getAnnotation(LinkedVertex.class);
                            String edgeLabel = linked == null ? NounHelper.isPlural(property) ? NounHelper.singularize(property) : property : linked.label();

                            Direction direction = linked == null ? OUT : linked.direction();
                            String statement = String.format("%s.%s(\"%s\").map(v -> (%s) new %s$Impl(%s.get())", fieldName, direction.toMethod(), edgeLabel, element.getSimpleName(), element.getSimpleName(), fieldName);

                            writer.beginMethod(returnType.toString(), executableEl.getSimpleName().toString(), modifiers)
                                    .emitStatement("return " + collectionType.wrap(statement))
                                    .endMethod();
                        } else if (edgeAnnotation != null) {
                            LinkedEdge linked = executableEl.getAnnotation(LinkedEdge.class);
                            Direction direction = linked == null ? OUT : linked.direction();

                            String statement = String.format("%s.%sE(\"%s\").map(%s -> (%s) new %s$Impl(%s.get())", fieldName, direction.toMethod(), edgeAnnotation.label(), fieldName, element.getSimpleName(), element.getSimpleName(), fieldName);

                            writer.beginMethod(returnType.toString(), executableEl.getSimpleName().toString(), modifiers)
                                    .emitStatement("return " + collectionType.wrap(statement))
                                    .endMethod();
                        } else {
                            throw new IllegalArgumentException("Not supported, non-edge collection type: " + returnType);
                        }
                    } else if (isVertex(executableEl.getReturnType())) {
                        boolean in = executableEl.getAnnotation(In.class) != null;

                        Element element = types.asElement(executableEl.getReturnType());
                        writer.beginMethod(executableEl.getReturnType().toString(), executableEl.getSimpleName().toString(), modifiers)
                                .emitStatement("return " + fieldName + "." + (in ? "in" : "out") + "V().map(v -> new " + element.getSimpleName() + "$Impl(v.get())).next()")
                                .endMethod();
                    } else {
                        String className;

                        boolean isPrimitive = executableEl.getReturnType().getKind().isPrimitive();
                        if (isPrimitive) {
                            className = primitiveToClass(executableEl.getReturnType());
                        } else {
                            className = executableEl.getReturnType().toString();
                        }


                        String propertyName = "\"" + property + "\"";
                        propertyName = hidden ? "com.tinkerpop.gremlin.structure.Graph.Key.hide(" + propertyName + ")" : propertyName;

                        writer.beginMethod(executableEl.getReturnType().toString(), executableEl.getSimpleName().toString(), modifiers)
                                .emitStatement("return %s.<%s>property(%s).orElse(%s)", fieldName, className, propertyName, getDefaultValue(executableEl.getReturnType()))
                                .endMethod();
                    }


                } else if (isSetter(executableEl)) {
                    String property = getPropertyName(executableEl);
                    Set<Modifier> modifiers = new HashSet<>(el.getModifiers());
                    modifiers.remove(ABSTRACT);

                    String propertyName = "\"" + property + "\"";
                    boolean hidden = isHiddenProperty(executableEl);
                    propertyName = hidden ? "com.tinkerpop.gremlin.structure.Graph.Key.hide(" + propertyName + ")" : propertyName;

                    TypeMirror propertyType = executableEl.getParameters().get(0).asType();
                    writer.beginMethod("void", executableEl.getSimpleName().toString(), modifiers, propertyType.toString(), property);
                    boolean isPrimitive = propertyType.getKind().isPrimitive();
                    if (isPrimitive) {
                        writer.emitStatement(fieldName + ".singleProperty(%s, %s)", propertyName, property);
                    } else {
                        writer.beginControlFlow("if (" + property + " == null)")
                                .emitStatement(fieldName + ".property(%s).remove()", propertyName)
                                .nextControlFlow("else")
                                .emitStatement(fieldName + ".singleProperty(%s, %s)", propertyName, property)
                                .endControlFlow();
                    }
                    writer.endMethod();
                }
            }
        }
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

    public void generateEdgeImplementationClass(TypeElement type) {
        messager.printMessage(OTHER, "Generating " + type.getQualifiedName() + "$Impl");

        try (PrintWriter out = new PrintWriter(filer.createSourceFile(type.getQualifiedName() + "$Impl").openOutputStream())) {
            JavaWriter writer = new JavaWriter(out);
            PackageElement packageEl = (PackageElement) type.getEnclosingElement();
            writer.emitPackage(packageEl.getQualifiedName().toString())
                    .emitImports(com.tinkerpop.gremlin.structure.Edge.class, FramedEdge.class)
                    .emitEmptyLine()
                    .beginType(type.getQualifiedName() + "$Impl", "class", EnumSet.of(PUBLIC, Modifier.FINAL), type.getQualifiedName().toString())
                    .emitField(com.tinkerpop.gremlin.structure.Edge.class.getName(), "e", EnumSet.of(PRIVATE))
                    .beginConstructor(EnumSet.of(PUBLIC), "Edge", "e")
                    .emitStatement("this.e = e")
                    .endConstructor()
                    .beginMethod(com.tinkerpop.gremlin.structure.Edge.class.getName(), "edge", EnumSet.of(PUBLIC))
                    .emitStatement("return e")
                    .endMethod();

            implementAbstractMethods(type, "e", writer);

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
        log.info("Check if {} is of collection type", type);
        if (type instanceof DeclaredType) {
            DeclaredType declaredType = (DeclaredType) type;

            boolean classOrInterface = declaredType.asElement().getKind().equals(CLASS) || declaredType.asElement().getKind().equals(INTERFACE);
            if (classOrInterface) {
                TypeElement element = (TypeElement) declaredType.asElement();
                log.info("Collection type is {}", element);
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
        return "get".equals(el.getSimpleName().subSequence(0, 3)) && el.getParameters().isEmpty() && el.getModifiers().contains(ABSTRACT);
    }

    private boolean isSetter(ExecutableElement el) {
        return "set".equals(el.getSimpleName().subSequence(0, 3)) && el.getParameters().size() == 1 && el.getReturnType().getKind() == VOID && el.getModifiers().contains(ABSTRACT);
    }

    private String getPropertyName(ExecutableElement el) {
        String property = el.getSimpleName().toString().substring(3);
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
