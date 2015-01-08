package peapod.demo.crew;

import peapod.FramedVertex;
import peapod.annotations.Property;
import peapod.annotations.Vertex;

/**
 * Created by Willem on 26/12/2014.
 */
@Vertex
public abstract class Person implements FramedVertex {

    public abstract String getName();

    @Property(hidden = true)
    public abstract boolean getVisible();

}

