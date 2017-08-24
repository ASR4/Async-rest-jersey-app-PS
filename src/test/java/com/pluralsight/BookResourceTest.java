package com.pluralsight;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.grizzly.connector.GrizzlyConnectorProvider;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Before;
import org.junit.Test;


import javax.ws.rs.client.Entity;
import javax.ws.rs.core.*;

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
        //Adding grizzly connector (see pom) instead of default JDK http connector for PATCH to work
        clientConfig.connectorProvider(new GrizzlyConnectorProvider());
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

    //Test for If-None-Match(if Etags don't match go to method else throw 304 not modified)/Conditional GET
    @Test
    public void BookEntityTagNotModified() {
        //Client side Etag
        EntityTag entityTag = target("books").path(book1_id).request().get().getEntityTag();
        assertNotNull(entityTag);

        Response response = target("books").path(book1_id).request().header("If-None-Match", entityTag).get();
        //304 not modified, as the client and server side Etags match
        assertEquals(304, response.getStatus());
    }

    @Test
    public void UpdateBookAuthor() {
        HashMap<String, Object> updates = new HashMap<String, Object>();
        updates.put("author", "updatedAuthor");
        Entity<HashMap<String, Object>> updateEntity = Entity.entity(updates, MediaType.APPLICATION_JSON);
        //Since PATCH does not have a dedicated get() or post() method we use build().invoke()
        Response updateResponse = target("books").path(book1_id).request().build("PATCH", updateEntity).invoke();

        assertEquals(200, updateResponse.getStatus());

        Response getResponse = target("books").path(book1_id).request().get();
        HashMap<String, Object> getResponseMap = toHashMap(getResponse);

        assertEquals("updatedAuthor", getResponseMap.get("author"));
    }

    //Test for PATCH with extra params(getting added by @JasonAnySetter)
    @Test
    public void UpdateBookExtra() {
        HashMap<String, Object> updates = new HashMap<String, Object>();
        updates.put("hello", "world");
        Entity<HashMap<String, Object>> updateEntity = Entity.entity(updates, MediaType.APPLICATION_JSON);
        //Since PATCH does not have a dedicated get() or post() method we use build().invoke()
        Response updateResponse = target("books").path(book1_id).request().build("PATCH", updateEntity).invoke();

        assertEquals(200, updateResponse.getStatus());

        Response getResponse = target("books").path(book1_id).request().get();
        HashMap<String, Object> getResponseMap = toHashMap(getResponse);

        assertEquals("world", getResponseMap.get("hello"));
    }

    //Test for PATCH If-Match(if Etags match go to method else throw 412 Precondition failed)
    @Test
    public void UpdateIfMatch() {
        //Create client side entityTag which is compared to the server side.
        EntityTag entityTag = target("books").path(book1_id).request().get().getEntityTag();

        HashMap<String, Object> updates = new HashMap<String, Object>();
        updates.put("author", "updatedAuthor");
        Entity<HashMap<String,Object>> updateEntity = Entity.entity(updates, MediaType.APPLICATION_JSON);
        Response updateResponse = target("books").path(book1_id).request()
                .header("If-Match", entityTag).build("PATCH", updateEntity).invoke();

        //If-Match, does not fail as the Etags match and hence a new response is created with a different server side Etag
        assertEquals(200, updateResponse.getStatus());

        Response updateResponse2 = target("books").path(book1_id).request().
                header("If-Match", entityTag).build("PATCH", updateEntity).invoke();

        //Because a different server side Etag is there now as the book object has been updated,
        //the If-Match fails and 412 is thrown
        System.out.println(updateResponse2.getStatus());
        assertEquals(412, updateResponse2.getStatus());
    }

    //Test for Http Override Filter on the Patch request
    @Test
    public void PatchMethodOverride() {
        HashMap<String, Object> updates = new HashMap<String, Object>();
        updates.put("author", "updateAuthor");
        Entity<HashMap<String, Object>> updateEntity = Entity.entity(updates, MediaType.APPLICATION_JSON);
        //The Http Override Filter registered in BookApplication sees queryParam("_method","PATCH")
        // and tells the request to use the PATCH annotated resource method
        Response updateResponse = target("books").path(book1_id).queryParam("_method", "PATCH").
                request().post(updateEntity);

        assertEquals(200, updateResponse.getStatus());

        Response getResponse = target("books").path(book1_id).request().get();
        HashMap<String, Object> getResponseMap = toHashMap(getResponse);

        assertEquals("updateAuthor", getResponseMap.get("author"));
    }

    //Test for Uri Filter
    @Test
    public void ContentNegotiationExtensions() {
        Response xmlResponse = target("books").path(book1_id + ".xml").request().get();
        assertEquals(MediaType.APPLICATION_XML, xmlResponse.getHeaderString("Content-Type"));

        Response jsonResponse = target("books").path(book1_id + ".json").request().get();
        assertEquals(MediaType.APPLICATION_JSON, jsonResponse.getHeaderString("Content-Type"));

    }

    //To test custom response filter
    @Test
    public void PoweredByHeader() {
        Response response = target("books").path(book1_id).request().get();
        assertEquals("Pluralsight", response.getHeaderString("X-Powered-By"));
    }
}
