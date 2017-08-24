package com.pluralsight;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/**
 * This is a custom Response Filter class, we are using this class to add a new
 * response header called "X-Powered-By"
 */
//@Provider so that jersey can find this class and so that we don't need to register/create
//a configuration/bean for this
@Provider
public class PoweredByFilter implements ContainerResponseFilter{
    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        responseContext.getHeaders().add("X-Powered-By", "Pluralsight");
    }
}
