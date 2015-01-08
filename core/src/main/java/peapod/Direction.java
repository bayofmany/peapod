package peapod;

/**
 * Created by wisa on 08/01/2015.
 */
public enum Direction {

    IN, OUT, BOTH;

    public String toMethod() {
        return name().toLowerCase();
    }
}
