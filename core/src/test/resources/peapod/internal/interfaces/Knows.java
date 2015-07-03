package peapod.internal.interfaces;

import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;


@Edge
public interface Knows {

    @Out
    Person getPerson();

    @In
    @SuppressWarnings("unused")
    Person getOtherPerson();

}