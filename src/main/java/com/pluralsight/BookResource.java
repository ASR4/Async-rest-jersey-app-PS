package com.pluralsight;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import org.apache.commons.codec.digest.DigestUtils;
import org.glassfish.jersey.server.ManagedAsync;

import javax.print.attribute.standard.Media;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.*;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.*;
import java.util.Collection;

/**
 * Created by Ashmeet
 * Jersey Test Project
 */
@Path("/books")
public class BookResource {

    //BookDao dao = new BookDao();
    @Context
    BookDao dao;

    @Context
    Request request;


    @GET
//  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //to set json as default
    @Produces({"application/json;qs=1", "application/xml;qs=0.5"})
    @ManagedAsync
    public void getBooks(@Suspended final AsyncResponse response){
       // response.resume(dao.getBooks());
        ListenableFuture< Collection<Book>> bookFuture = dao.getBooksAsync();
        Futures.addCallback(bookFuture, new FutureCallback< Collection<Book>>() {
            @Override
            public void onSuccess( Collection<Book> addedBook) {
                response.resume(addedBook);
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    //Conditional GET/Caching/Entity Tag
    @Path("/{id}")
    @GET
//  @Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    //to set json as default
    @Produces({"application/json;qs=1", "application/xml;qs=0.5"})
    @ManagedAsync
    //https://stackoverflow.com/questions/11138215/entitytag-value-caching-comparison-how-to-in-jersey
    public void getBook(@PathParam("id") String id, @Suspended final AsyncResponse response){
        //response.resume(dao.getBook(id));
        ListenableFuture<Book> bookFuture = dao.getBookAsync(id);
        Futures.addCallback(bookFuture, new FutureCallback<Book>() {
            @Override
            public void onSuccess(Book addedBook) {
               // response.resume(addedBook);
               // To check if the response has been changed (Conditional GET), generate a unique tag
               EntityTag entityTag = generateEntityTag(addedBook);
                // the following method call will result in Jersey checking the headers of the
                // incoming request, comparing them with the entity tag generated for
                // the current version of the resource generates "304 Not Modified" response
                // if the same. Otherwise returns null.
               Response.ResponseBuilder rb = request.evaluatePreconditions(entityTag);
               //If response has not changed
               if (rb != null) {
                   //304 not modified
                   response.resume(rb.build());
               //If response has been modified
               } else {
                   // return the current version of the resource with the corresponding tag
                   response.resume(Response.ok().tag(entityTag).entity(addedBook).build());
               }
            }
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void addBook(@Valid @NotNull Book book, @Suspended final AsyncResponse response) {
        //response.resume(dao.addBook(book));
        //async call through DAL using guava
        ListenableFuture<Book> bookFuture = dao.addBookAsync(book);
        //async call by jersey
        Futures.addCallback(bookFuture, new FutureCallback<Book>() {
            @Override
            public void onSuccess(Book addedBook) {
                response.resume(addedBook);
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    //For PATCH annotation use case
    @Path("/{id}")
    @PATCH
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    @ManagedAsync
    public void updateBook(@PathParam("id") String id, Book book, @Suspended final AsyncResponse response) {
        ListenableFuture<Book> bookFuture = dao.updateBookAsync(id, book);
        Futures.addCallback(bookFuture, new FutureCallback<Book>() {
            @Override
            public void onSuccess(Book updatedBook) {
                response.resume(updatedBook);
            }

            @Override
            public void onFailure(Throwable throwable) {
                response.resume(throwable);
            }
        });
    }

    //For conditional Get, it is basically caching of the response, in a new entity.
    EntityTag generateEntityTag(Book book) {
        return new EntityTag(DigestUtils.md5Hex(book.getAuthor() +
        book.getTitle() + book.getPublished() + book.getExtras()));
    }

 }
