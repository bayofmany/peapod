package peapod.impl;

/**
 * Created by wisa on 07/01/2015.
 */
public class NounHelper {

    public static String singularize(String input) {
        if (input.endsWith("s")) {
            return input.substring(0, input.length() - 1);
        }
        return input;
    }

    public static boolean isPlural(String input) {
        return input.endsWith("s");
    }
}
