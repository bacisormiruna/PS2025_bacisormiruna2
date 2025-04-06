package com.example.demo.errorhandler;

public class FriendshipException extends DemoException {
    public FriendshipException(String message) {
        super(message);
    }

    public FriendshipException(String message, Throwable cause) {
        super(message, cause);
    }
}
