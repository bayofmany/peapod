package peapod.testcases.outedge;

import peapod.annotations.Vertex;
import java.util.List;

/**
 * Created by Willem on 28/12/2014.
 */
@Vertex
public abstract class Person {

    public abstract List<Knows> getKnowsEdge();

}
