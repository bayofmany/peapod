package peapod.internal.classes;

import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;

@Edge
public abstract class Knows {

    @Out
    public abstract Person getPerson();

    @In
    @SuppressWarnings("unused")
    public abstract Person getOtherPerson();

}
