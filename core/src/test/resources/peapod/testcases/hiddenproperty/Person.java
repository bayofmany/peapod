package peapod.testcases.hiddenproperty;

import peapod.annotations.Property;
import peapod.annotations.Vertex;

@Vertex
public abstract class Person {

    @Property(hidden = true)
    public abstract String getAcl();

}
