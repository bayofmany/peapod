package peapod.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by Willem on 26/12/2014.
 */
@Retention(RUNTIME)
@Target(TYPE)
public @interface Edge {

    String label() default com.tinkerpop.gremlin.structure.Edge.DEFAULT_LABEL;

}
