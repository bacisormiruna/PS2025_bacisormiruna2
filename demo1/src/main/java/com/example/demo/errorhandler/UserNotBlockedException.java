package com.example.demo.errorhandler;

public class UserNotBlockedException extends RuntimeException{
    public UserNotBlockedException(String message) {
        super(message);
    }
}
