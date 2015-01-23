package peapod.internal;

import peapod.annotations.Vertex;
import java.util.List;

@Vertex
public abstract class Person {

    public abstract String getName();

    public abstract List<Knows> getKnows();

}
