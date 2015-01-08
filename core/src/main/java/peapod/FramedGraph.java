package peapod;

import com.tinkerpop.gremlin.process.T;
import com.tinkerpop.gremlin.structure.Graph;
import com.tinkerpop.gremlin.structure.Graph.Features;
import com.tinkerpop.gremlin.structure.Graph.Variables;
import com.tinkerpop.gremlin.structure.Transaction;
import com.tinkerpop.gremlin.structure.Vertex;
import org.apache.commons.configuration.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.NoSuchElementException;

/**
 * A framed instance of a tinkerpop graph.
 * Created by Willem on 26/12/2014.
 */
public class FramedGraph implements AutoCloseable {

    private Graph graph;

    public FramedGraph(Graph graph) {
        this.graph = graph;
    }

    /**
     * Add a linkedvertex of type {@link V} to the graph. The label will be the lowercase value of the class.
     *
     * @param clazz a class implementing {@link FramedVertex} and annotated with {@link com.tinkerpop.gremlin.structure.Vertex}
     * @return The newly created labeled linkedvertex
     */
    public <V> V addVertex(Class<V> clazz) {
        Vertex v = graph.addVertex(toLabel(clazz));
        return createInstance(clazz, v);
    }

    /**
     * Add a linkedvertex of type {@link V} and given id to the graph. The label will be the lowercase value of the class.
     *
     * @param clazz a class implementing {@link FramedVertex} and annotated with {@link com.tinkerpop.gremlin.structure.Vertex}
     * @return The newly created labeled linkedvertex
     */
    public <V> V addVertex(Class<V> clazz, Object id) {
        Vertex v = graph.addVertex(T.id, id, T.label, toLabel(clazz));
        return createInstance(clazz, v);
    }

    /**
     * Get a {@link Vertex} given its unique identifier.
     *
     * @param id The unique identifier of the linkedvertex to locate
     * @throws NoSuchElementException if the linkedvertex is not found.
     */
    @SuppressWarnings("unchecked")
    public <T> T v(Object id, Class<T> clazz)  throws NoSuchElementException {
        return createInstance(clazz, graph.v(id));
    }

    private <V> String toLabel(Class<V> clazz) {
        return clazz.getSimpleName().toLowerCase();
    }

    private <V> V createInstance(Class<V> clazz, Vertex v) {
        try {
            Class<?> framingClass = clazz.getClassLoader().loadClass(clazz.getName() + "$Impl");
            Constructor<?> constructor = framingClass.getConstructor(Vertex.class);
            return (V) constructor.newInstance(v);
        } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @see com.tinkerpop.gremlin.structure.Graph#tx()
     */
    public Transaction tx() {
        return graph.tx();
    }

    /**
     * @see com.tinkerpop.gremlin.structure.Graph#variables()
     */
    public Variables variables() {
        return graph.variables();
    }

    /**
     * @see com.tinkerpop.gremlin.structure.Graph#configuration()
     */
    public Configuration configuration() {
        return graph.configuration();
    }

    /**
     * @see com.tinkerpop.gremlin.structure.Graph#features()
     */
    public Features features() {
        return graph.features();
    }

    /**
     * @return the underlying {@link com.tinkerpop.gremlin.structure.Graph}
     */
    public Graph graph() {
        return graph;
    }

    @Override
    public void close() throws Exception {
        graph.close();
    }
}
