package peapod.edge;

import peapod.annotations.LinkedEdge;
import peapod.annotations.LinkedVertex;
import peapod.annotations.Vertex;

import java.util.List;

import static peapod.Direction.BOTH;
import static peapod.Direction.IN;
import static peapod.Direction.OUT;

/**
 * Created by Willem on 2/01/2015.
 */
@Vertex
public abstract class Person {

    public abstract String getName();

    public abstract List<Friend> getFriends();

    @LinkedEdge
    public abstract List<Friend> getFriendsWithAnnotationDefault();

    @LinkedEdge(direction = OUT)
    public abstract List<Friend> getFriendsWithAnnotationOut();

    @LinkedEdge(direction = IN)
    public abstract List<Friend> getFriendsWithAnnotationIn();

    @LinkedEdge(direction = BOTH)
    public abstract List<Friend> getFriendsWithAnnotationBoth();


}
