package peapod.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Willem on 26/12/2014.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Property {

    boolean hidden() default false;
}
