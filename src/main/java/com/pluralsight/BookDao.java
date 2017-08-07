package com.pluralsight;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class BookDao {

    private Map<String, Book> books;

    BookDao(){
        books = new HashMap<>();

        Book book1 = new Book();
        book1.setId("1");
        book1.setTitle("Title1");
        book1.setAuthor("Author1");
        book1.setIsbn("1234");
        book1.setPublished(new Date());

        Book book2 = new Book();
        book2.setId("2");
        book2.setTitle("Title2");
        book2.setAuthor("Author2");
        book2.setIsbn("2345");
        book2.setPublished(new Date());

        books.put(book1.getId(), book1);
        books.put(book2.getId(), book2);
    }

    Collection<Book> getBooks(){
        return books.values();
    }

    Book getBook(String id){
        return books.get(id);
    }
}
