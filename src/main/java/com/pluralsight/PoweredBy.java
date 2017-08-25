package com.pluralsight;

import javax.ws.rs.NameBinding;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class to create name binding annotations that will be used to allow Filters
 * to work only where called for by this name binding annotation, rather than for every response
 */
@NameBinding
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PoweredBy {
    //This will allow us to add a string whenever this
    //annotation will be used on a resource method
    String value() default "";
}
