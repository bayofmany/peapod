package peapod.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.RetentionPolicy.SOURCE;

/**
 * Created by Willem on 26/12/2014.
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Out {

}
