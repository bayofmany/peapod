package peapod.demo.classic;

import peapod.FramedVertex;
import peapod.annotations.In;
import peapod.annotations.Out;
import peapod.annotations.Vertex;

/**
 * Created by Willem on 26/12/2014.
 */
@Vertex
public abstract class Software implements FramedVertex {

    public abstract String getName();

    public abstract String getLang();

    @Out
    public abstract Person getPerson();

    @In
    public abstract Software getSoftware();

}

