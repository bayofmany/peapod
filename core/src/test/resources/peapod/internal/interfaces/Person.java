package peapod.internal.interfaces;

import peapod.FramedVertex;
import peapod.annotations.Edge;
import peapod.annotations.Property;
import peapod.annotations.Vertex;

import java.util.List;

@Vertex
public interface Person {

    String getName();

    List<Knows> getKnows();

}
