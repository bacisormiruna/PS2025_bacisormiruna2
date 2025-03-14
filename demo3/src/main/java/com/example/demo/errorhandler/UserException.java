package com.example.demo.errorhandler;

public class UserException extends DemoException {
    public UserException(String message) {
        super(message);
    }

    public UserException (String message, Throwable cause) {
        super(message, cause);
    }
}
