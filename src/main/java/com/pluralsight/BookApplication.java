package com.pluralsight;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.ResourceConfig;

public class BookApplication extends ResourceConfig{

    BookApplication(final BookDao dao) {
        packages("com.pluralsight");
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                bind(dao).to(BookDao.class);
            }
        });
    }
}
