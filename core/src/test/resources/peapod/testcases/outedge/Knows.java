package peapod.testcases.outedge;

import peapod.annotations.Edge;
import peapod.annotations.Vertex;
import peapod.annotations.Out;
import peapod.annotations.In;
import java.util.List;

/**
 * Created by Willem on 28/12/2014.
 */
@Edge(label = "knows")
public abstract class Knows {

    @Out
    public abstract Person getPerson();

    @In
    public abstract Person getOtherPerson();

}
