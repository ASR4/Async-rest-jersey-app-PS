package com.pluralsight;

import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

public class BookDao {

    private Map<String, Book> books;
    //Guava
    private ListeningExecutorService service;

    BookDao(){
        books = new ConcurrentHashMap<String, Book>();
        service = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    }

    Collection<Book> getBooks(){
        return books.values();
    }


    //Asynchronus call of getBooks using Guava
    ListenableFuture<Collection<Book>> getBooksAsync() {
        ListenableFuture<Collection<Book>> future = service.submit(new Callable<Collection<Book>>() {
            @Override
            public Collection<Book> call() throws Exception {
                return getBooks();
            }
        });
        return future;
    }

    Book getBook(String id) throws BookNotFoundException {
        if(books.containsKey(id)) {
            return books.get(id);
        } else {
            throw new BookNotFoundException("Book " + id + " is not found");
        }
    }

    //Asynchronus call of getBook using Guava
    ListenableFuture<Book> getBookAsync(final String id) {
        ListenableFuture<Book> future = service.submit(new Callable<Book>() {
            @Override
            public Book call() throws Exception {
                return getBook(id);
            }
        });
        return future;
    }

    //Post endpoint method
    Book addBook(Book book) {
        book.setId(UUID.randomUUID().toString());
        books.put(book.getId(), book);
        return book;
    }

    //Asynchronus call of addBook using Guava
    ListenableFuture<Book> addBookAsync(final Book book) {
        ListenableFuture<Book> future = service.submit(new Callable<Book>() {
            @Override
            public Book call() throws Exception {
                return addBook(book);
            }
        });
        return future;
    }

    //Method to use the PATCH annotation. Patch is used in cases where partial update is needed.
    Book updateBook(String id, Book updates) throws BookNotFoundException {
        if(books.containsKey(id)) {
            Book book = books.get(id);
            if (updates.getTitle() != null) {book.setTitle(updates.getTitle());}
            if (updates.getAuthor() != null) {book.setAuthor(updates.getAuthor());}
            if (updates.getPublished() != null) {book.setPublished(updates.getPublished());}
            if (updates.getExtras() != null) {
                for (String key : updates.getExtras().keySet()) {
                    book.set(key, updates.getExtras().get(key));
                }
            }
            return book;
        }
        else {
            throw new BookNotFoundException("Book " + id + " is not found");
        }
    }

    ListenableFuture<Book> updateBookAsync(final String id, final Book book) {
     ListenableFuture<Book> future = service.submit(new Callable<Book>() {
         @Override
         public Book call() throws Exception {
             return updateBook(id, book);
         }
     });
     return future;
    }
}
