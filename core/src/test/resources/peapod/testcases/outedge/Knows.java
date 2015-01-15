package peapod.testcases.outedge;

import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;

@Edge(label = "knows")
public abstract class Knows {

    @Out
    public abstract Person getPerson();

    @In
    public abstract Person getOtherPerson();

}
