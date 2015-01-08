package peapod.demo.crew;

import peapod.FramedVertex;
import peapod.annotations.Vertex;

/**
 * Created by Willem on 26/12/2014.
 */
@Vertex
public abstract class Software implements FramedVertex {

    public abstract String getName();

    public abstract void setName(String name);

    public abstract String getLang();

    public abstract void setLang(String lang);
}

