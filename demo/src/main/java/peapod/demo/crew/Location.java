package peapod.demo.crew;

import peapod.FramedVertexProperty;

/**
 * Represents a the metadata of the location.
 * Created by Willem on 26/12/2014.
 */
public abstract class Location implements FramedVertexProperty<String> {

    public abstract Integer getStartTime();

    public abstract void setStartTime(Integer startTime);

    public abstract Integer getEndTime();

    public abstract void setEndTime(Integer startTime);

}
