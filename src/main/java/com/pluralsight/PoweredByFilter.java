package com.pluralsight;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.annotation.Annotation;

/**
 * This is a custom Response Filter class, we are using this class to add a new
 * response header called "X-Powered-By"
 */
//@Provider so that jersey can find this class and so that we don't need to register/create
//a configuration/bean for this
@Provider
//This annotation is to indicate that it should only be invoked by resource method where this
//annotation is available
@PoweredBy
public class PoweredByFilter implements ContainerResponseFilter{

    //This method puts a filter on all the response headers to have "X-Powered-By" header
//    @Override
//    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
//        responseContext.getHeaders().add("X-Powered-By", "Pluralsight");
//    }

    //This method uses name binding annotations to put filters only where the resource method
    //has this annotation
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        //responseContext.getHeaders().add("X-Powered-By", "Pluralsight");
        //Changing from hardcoded X-Powered-By header to a variable denoted by the @PowerBy annotation
        for(Annotation a : responseContext.getEntityAnnotations()){
            if(a.annotationType() == PoweredBy.class) {
                String value = ((PoweredBy) a).value();
                responseContext.getHeaders().add("X-Powered-By", value);
            }
        }
    }
}
