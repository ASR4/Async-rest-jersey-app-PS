package com.pluralsight;

/**
 * Class to extend exception and pass the message to the super constructor
 */
public class BookNotFoundException extends Exception{

    BookNotFoundException(String p) {
        super(p);
    }
}
