package peapod.linkedvertex;

import peapod.FramedVertex;
import peapod.annotations.LinkedVertex;
import peapod.annotations.Vertex;

import java.util.List;

import static peapod.Direction.*;

/**
 * Test class to check @LinkedVertex constructs.
 * Created by Willem on 2/01/2015.
 */
@Vertex
public abstract class Person implements FramedVertex {

    public abstract String getName();

    public abstract List<Person> getFriends();

    @LinkedVertex(label = "friend")
    public abstract List<Person> getFriendsWithAnnotationDefault();

    @LinkedVertex(label = "friend", direction = OUT)
    public abstract List<Person> getFriendsWithAnnotationOut();

    @LinkedVertex(label = "friend", direction = IN)
    public abstract List<Person> getFriendsWithAnnotationIn();

    @LinkedVertex(label = "friend", direction = BOTH)
    public abstract List<Person> getFriendsWithAnnotationBoth();

}
