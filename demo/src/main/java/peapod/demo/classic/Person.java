package peapod.demo.classic;

import peapod.FramedVertex;
import peapod.annotations.Vertex;

import java.util.List;

/**
 * Created by Willem on 26/12/2014.
 */
@Vertex
public abstract class Person implements FramedVertex {

    public abstract String getName();

    public abstract Integer getAge();

    public abstract List<Person> getKnows();

    public abstract List<Knows> getKnowsEdge();

    public abstract List<Software> getCreated();

    public abstract List<Created> getCreatedEdge();

}

