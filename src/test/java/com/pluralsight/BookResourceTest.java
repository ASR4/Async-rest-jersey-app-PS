package com.pluralsight;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;


import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import static java.lang.System.in;
import static org.junit.Assert.*;

public class BookResourceTest extends JerseyTest{

    private String book1_id;
    private String book2_id;

    /**
     * Method to configure the container for Jersey Test
     * @return
     */
    protected Application configure() {
        //Logs http response headers
        enable(TestProperties.LOG_TRAFFIC);
        //Logs Http response body (in this case the json body)
        enable(TestProperties.DUMP_ENTITY);

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

    //Client config to prevent jersey from filling null map values.
    //Use case: to test @NotNull bean validation by passing null values in
    //addBook method
    protected void configureClient(ClientConfig clientConfig) {
        JacksonJsonProvider json = new JacksonJsonProvider().
                configure(SerializationFeature.WRITE_NULL_MAP_VALUES, false);
        clientConfig.register(json);
    }

    //Method to add books in DAL to be used for testing
    //converted from Book object to hasMap<String, Book> to make it more generic, so that various key value
    //can be added and not just things defined in the Book class (this is for testing only)
    protected Response addBook(String author, String title, Date published, String isbn, String... extras) {
        HashMap<String, Object> book = new HashMap<String, Object>();
        book.put("author", author);
        book.put("title", title);
        book.put("published", published);
        book.put("isbn", isbn);
        if(extras != null){
            int count = 1;
            for(String s: extras){
                book.put("extra" + count++, s);
            }
        }
        Entity< HashMap<String, Object>> bookEntity = Entity.entity(book, MediaType.APPLICATION_JSON_TYPE);
        return target("books").request().post(bookEntity);
    }

    //method to convert response to hashmap
    protected HashMap<String, Object> toHashMap(Response response) {
        return (response.readEntity(new GenericType<HashMap<String, Object>>() {}));
    }

    @Test
    public void testAddBook() throws ParseException {
        Date thisDate = new Date();

        Response response = addBook("author", "title", thisDate,"5678");
        assertEquals(200, response.getStatus());

        //To map a generic jersey response object to a specific custom object response
        HashMap<String, Object> responseBook = toHashMap(response);
        assertNotNull(responseBook.get("id"));
        assertEquals("title", responseBook.get("title"));
        assertEquals("author", responseBook.get("author"));

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        assertEquals(thisDate, dateFormat.parse(String.valueOf(responseBook.get("published"))));
        assertEquals("5678", responseBook.get("isbn"));

    }

    @Before
    public void setupBooks() {
        book1_id = (String)toHashMap(addBook("author1", "title1", new Date(),"1234")).get("id");
        book2_id = (String)toHashMap(addBook("author2", "title2", new Date(),"2345")).get("id");
    }

    @Test
    public void testGetBook() {
        HashMap<String, Object> response = toHashMap(target("books").path(book1_id).request().get());
        assertNotNull(response);
    }

    @Test
    public void testGetBooks() {
        Collection<HashMap<String, Object>> response = target("books").request().get(new GenericType<Collection<HashMap<String, Object>>>() {});
        assertEquals(2, response.size());
    }

    @Test
    public void testAddExtraField() {
        Response response = addBook("author", "title", new Date(), "1111", "hello world");
          assertEquals(200, response.getStatus());

          HashMap<String, Object> book = toHashMap(response);
          assertNotNull(book.get("id"));
          assertEquals(book.get("extra1"), "hello world");
    }

    @Test
    public void getBooksAsXml() {
        String output = target("books").request(MediaType.APPLICATION_XML).get(String.class);
        XML xml = new XMLDocument(output);

        assertEquals("author1", xml.xpath("/books/book[@id='" + book1_id + "']/author/text()").get(0));
        assertEquals("title1", xml.xpath("/books/book[@id='" + book1_id + "']/title/text()").get(0));

        assertEquals(2, xml.xpath("//book/author/text()").size());
    }

    @Test
    public void addBookNoAuthor() {
        Response response = addBook(null, "title1", new Date(), "1234");
        assertEquals(400, response.getStatus());
        String message = response.readEntity(String.class);
        assertTrue(message.contains("author is a required field"));
    }

    @Test
    public void addBookNoTitle() {
        Response response = addBook("author1", null, new Date(), "1234");
        assertEquals(400, response.getStatus());
        String message = response.readEntity(String.class);
        assertTrue(message.contains("title is a required field"));
    }

    @Test
    public void addBookNoBook() {
        Response response = target("books").request().post(null);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void BookNotFoundWithMessage() {
        Response response = target("books").path("1").request().get();
        assertEquals(404, response.getStatus());

        String message = response.readEntity(String.class);
        assertTrue(message.contains("Book 1 is not found"));
    }

}
