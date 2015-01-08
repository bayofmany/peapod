package peapod.testcases.outvertex;

import peapod.annotations.LinkedVertex;
import peapod.annotations.Vertex;
import java.util.List;

/**
 * Created by Willem on 28/12/2014.
 */
@Vertex
public abstract class Person {

    @LinkedVertex(label = "knows")
    public abstract List<Person> getKnows();

}
