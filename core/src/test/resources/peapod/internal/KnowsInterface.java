package peapod.internal;

import peapod.annotations.Edge;
import peapod.annotations.In;
import peapod.annotations.Out;


@Edge
public interface KnowsInterface {
  public Float getWeight();

  @Out 
  public PersonInterface getOutPerson();
  
  @In  
  public PersonInterface getInPerson();
}