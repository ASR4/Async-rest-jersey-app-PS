package com.pluralsight;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;

import java.util.Collection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class BookResourceTest extends JerseyTest{

    /**
     * Method to configure the container for Jersey Test
     * @return
     */
    protected Application configure() {
        //Logs http response headers
        enable(TestProperties.LOG_TRAFFIC);
        //Logs Http response body (in this case the json body)
        enable(TestProperties.DUMP_ENTITY);

        final BookDao dao = new BookDao();
        //Same job as the ResourceConfig in  Main class
        return new ResourceConfig().packages("com.pluralsight").
                register(new AbstractBinder() {
                    @Override
                    protected void configure() {
                        bind(dao).to(BookDao.class);
                    }
                });
    }

    @Test
    public void testGetBook() {
        Book response = target("books").path("1").request().get(Book.class);
        assertNotNull(response);
    }

    @Test
    public void testGetBooks() {
        Collection<Book> response = target("books").request().get(new GenericType<Collection<Book>>() {});
        assertEquals(2, response.size());
    }

    @Test
    public void testDao() {
        Book response1 = target("books").path("1").request().get(Book.class);
        Book response2 = target("books").path("1").request().get(Book.class);
        assertEquals(response1.getPublished(), response2.getPublished());
    }
}
