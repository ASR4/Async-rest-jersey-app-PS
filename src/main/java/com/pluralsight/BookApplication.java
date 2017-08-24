package com.pluralsight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.server.filter.HttpMethodOverrideFilter;
import org.glassfish.jersey.server.filter.UriConnegFilter;

import javax.ws.rs.core.MediaType;
import java.util.HashMap;

/**
 * Class to create and configure all the beans
 */
public class BookApplication extends ResourceConfig{

    BookApplication(final BookDao dao) {

        //Creating a new jackson object and configuring it with desirables
        JacksonJsonProvider json = new JacksonJsonProvider().
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).
                configure(SerializationFeature.INDENT_OUTPUT, true);

        //Creating a jackson obkect for XML
        JacksonXMLProvider xml = new JacksonXMLProvider().
                configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false).
                configure(SerializationFeature.INDENT_OUTPUT, true);

        //Map for creating URI filter(This filter allows client to choose media type
        //directly from the endpoint rather than mentioning it in the accept header)
        HashMap<String, MediaType> mappings = new HashMap<String, MediaType>();
        mappings.put("json",  MediaType.APPLICATION_JSON_TYPE);
        mappings.put("xml", MediaType.APPLICATION_XML_TYPE);
        UriConnegFilter uriConnegFilter = new UriConnegFilter(mappings, null);

        //packages to scan
        packages("com.pluralsight");

        //registering a binder using the BookDao object as a bean

        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(dao).to(BookDao.class);
            }
        });

        //registering json object as a bean
        register(json);

        //registering xml object as a bean
        register(xml);

        //For bean validation to be able to return message
        property(ServerProperties.BV_SEND_ERROR_IN_RESPONSE, true);

        //To register Http Overide filter (This overloads the POST endpoint
        //with less used verbs like DELETE and PUT)
        register(HttpMethodOverrideFilter.class);

        //To register the Uri Filter
        register(uriConnegFilter);
    }
}
