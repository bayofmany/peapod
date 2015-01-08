package peapod.testcases.hiddenproperty;

import peapod.annotations.Property;
import peapod.annotations.Vertex;

/**
 * Created by Willem on 28/12/2014.
 */
@Vertex
public abstract class Person {

    @Property(hidden = true)
    public abstract String getAcl();

}
