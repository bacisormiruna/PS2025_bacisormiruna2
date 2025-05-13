package com.example.demo.errorhandler;


public class AlreadyReactedException extends RuntimeException {
    public AlreadyReactedException(String message) {
        super(message);
    }
}
