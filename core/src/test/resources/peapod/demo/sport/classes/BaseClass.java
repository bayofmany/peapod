package peapod.demo.sport.classes;


import peapod.annotations.Property;
import peapod.annotations.Vertex;
import peapod.demo.sport.Base;
import peapod.demo.sport.Tokens;

@Vertex
public interface BaseClass extends Base {

    @Property(Tokens.NAME)
    public void  setName(final String name);

    @Property(Tokens.NAME)
    public String getName();

    @Property(Tokens.TYPE)
    public String getType();

    @Property(Tokens.TYPE)
    public void setType(final String type);

    @Property(Tokens.DESCRIPTION)
    public String getDescription();

    @Property(Tokens.DESCRIPTION)
    public void setDescription(final String description);

    @Property(Tokens.STARTDATE)
    public void setStartDate(final long startDate);

    @Property(Tokens.STARTDATE)
    public long getStartDate();

    @Property(Tokens.ENDDATE)
    public void setEndDate(final long endDate);

    @Property(Tokens.ENDDATE)
    public long getEndDate();
}
