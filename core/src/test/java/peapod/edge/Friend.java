package peapod.edge;

import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;

/**
 * Created by wisa on 08/01/2015.
 */
@Edge(label = "friend")
public abstract class Friend {

    public abstract int getStartYear();

    @Out
    public abstract Person getPerson();

    @In
    public abstract Person getFriend();

}
