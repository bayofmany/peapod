package peapod.demo.classic;

import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;

/**
 * Created by Willem on 1/01/2015.
 */
@Edge(label = "knows")
public abstract class Knows {

    public abstract Float getWeight();

    @Out
    public abstract Person getPerson();

    @In
    public abstract Person getOtherPerson();

}
