package peapod.model;

import peapod.FramedVertex;
import peapod.annotations.Vertex;

/**
 * Created by Willem on 2/01/2015.
 */
@Vertex
public abstract class Person implements FramedVertex {

    public abstract String getName();

}
