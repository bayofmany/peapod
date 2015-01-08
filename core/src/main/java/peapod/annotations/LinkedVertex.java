package peapod.annotations;

import peapod.Direction;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Willem on 26/12/2014.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface LinkedVertex {

    String label();

    Direction direction() default Direction.OUT;

}
