package com.pluralsight;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.Providers;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collection;

/**
 * Class to wrap the book xml response. In this eg: we are wrapping with the tag <books>
 * <books>
 *      <book>
 *          <title>"title1"<title/>
 *      <book/>
 *      <book>
 *          <title>"title2"<title/>
 *      <book/>
 * <books/>
 */
@Provider
@Produces(MediaType.APPLICATION_XML)
public class BooksMessageBodyWriter implements MessageBodyWriter<Collection<Book>>{

    //Class to create the wrapper
    @JacksonXmlRootElement(localName = "books")
    public class BooksWrapper{

        //False cause we are using an actual class(above) to do the wrapping
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "book")
        public Collection<Book> books;

        BooksWrapper(Collection<Book> books) {
            this.books = books;
        }
    }

    //Gives access to all the providers configured in this application
    @Context
    Providers providers;

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {

        //Use this message writer if the class type is collection(because only one resource
        //method getBooks will be used in this scenario)
        return Collection.class.isAssignableFrom(type);
    }

    @Override
    public long getSize(Collection<Book> books, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Collection<Book> books, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {

        providers.getMessageBodyWriter(BooksWrapper.class, genericType, annotations, mediaType).
                writeTo(new BooksWrapper(books), type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
}
