package com.example.demo.errorhandler;

public class UserAlreadyBlockedException extends RuntimeException{
    public UserAlreadyBlockedException(String message) {
        super(message);
    }
}
