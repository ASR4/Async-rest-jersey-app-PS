package com.pluralsight;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.jaxrs.xml.JacksonXMLProvider;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

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

        //packages to scan
        packages("com.pluralsight");

        //registering BookDao object as a bean
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
    }
}
