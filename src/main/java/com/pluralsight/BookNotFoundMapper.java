package com.pluralsight;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

//@Provider so that jersey can find this class and so that we don't need to register/create
//a configuration/bean for this
@Provider
public class BookNotFoundMapper implements ExceptionMapper<BookNotFoundException>{

    public Response toResponse(BookNotFoundException ex) {
        return Response.status(404).entity(ex.getMessage()).type("text/plain").build();
    }
}
