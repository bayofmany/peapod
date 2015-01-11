package peapod.testcases.outvertex;

import peapod.annotations.LinkedVertex;
import peapod.annotations.Vertex;
import java.util.List;

@Vertex
public abstract class Person {

    @LinkedVertex(label = "knows")
    public abstract List<Person> getKnows();

    public abstract void addKnows(Person person);

}
