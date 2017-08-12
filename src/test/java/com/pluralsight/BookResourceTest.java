package com.pluralsight;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.junit.Before;
import org.junit.Test;


import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class BookResourceTest extends JerseyTest{

    private String book1_id;
    private String book2_id;

    /**
     * Method to configure the container for Jersey Test
     * @return
     */
    protected Application configure() {
        //Logs http response headers
        //enable(TestProperties.LOG_TRAFFIC);
        //Logs Http response body (in this case the json body)
        //enable(TestProperties.DUMP_ENTITY);

//        final BookDao dao = new BookDao();
//        //Same job as the ResourceConfig in  Main class
//        return new ResourceConfig().packages("com.pluralsight").
//                register(new AbstractBinder() {
//                    @Override
//                    protected void configure() {
//                        bind(dao).to(BookDao.class);
//                    }
//                });
        final BookDao dao = new BookDao();
        return new BookApplication(dao);
    }

    //Method to add books in DAL to be used for testing
    protected Response addBook(String author, String title, Date published, String isbn) {
        Book book = new Book();
        book.setAuthor(author);
        book.setTitle(title);
        book.setPublished(published);
        book.setIsbn(isbn);
        Entity<Book> bookEntity = Entity.entity(book, MediaType.APPLICATION_JSON_TYPE);
        return target("books").request().post(bookEntity);
    }

    @Test
    public void testAddBook() {
        Date thisDate = new Date();

        Response response = addBook("author", "title", thisDate,"5678");
        assertEquals(200, response.getStatus());

        //To map a generic jersey response object to a specific custom object response
        Book responseBook = response.readEntity(Book.class);
        assertNotNull(responseBook.getId());
        assertEquals("title", responseBook.getTitle());
        assertEquals("author", responseBook.getAuthor());
        assertEquals(thisDate, responseBook.getPublished());
        assertEquals("5678", responseBook.getIsbn());

    }

    @Before
    public void setupBooks() {
        book1_id = addBook("author1", "title1", new Date(),"1234").readEntity(Book.class).getId();
        book2_id = addBook("author2", "title2", new Date(),"2345").readEntity(Book.class).getId();
    }

    @Test
    public void testGetBook() {
        Book response = target("books").path(book1_id).request().get(Book.class);
        assertNotNull(response);
    }

    @Test
    public void testGetBooks() {
        Collection<Book> response = target("books").request().get(new GenericType<Collection<Book>>() {});
        assertEquals(2, response.size());
    }

}
