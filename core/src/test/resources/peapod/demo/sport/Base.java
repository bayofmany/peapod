package peapod.demo.sport;

import peapod.annotations.Property;
import peapod.annotations.Vertex;


public interface Base {

    @Property(Tokens.CREATIONDATE)
    public void setCreationDate(final long creationDate);

    @Property(Tokens.CREATIONDATE)
    public Long getCreationDate();

    @Property(Tokens.SOURCE)
    public String getSource();

    @Property(Tokens.SOURCE)
    public void setSource(final String source);

}
