package peapod.model;

import peapod.FramedVertex;
import peapod.annotations.Vertex;

import java.util.List;

@Vertex
public abstract class Person implements FramedVertex {

    public abstract String getName();

    public abstract List<Person> getFriends();


}
