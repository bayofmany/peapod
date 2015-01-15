package peapod.testcases.outedge;

import peapod.annotations.Vertex;
import java.util.List;

@Vertex
public abstract class Person {

    public abstract List<Knows> getKnowsEdge();

}
