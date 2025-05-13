package com.example.demo.errorhandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class ExceptionControllerHandler {

    private final Logger LOGGER = LoggerFactory.getLogger(ExceptionControllerHandler.class);

    @ExceptionHandler(UserException.class)
    public ResponseEntity<HttpErrorResponse> userException(UserException exception)
    {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(FriendshipException.class)
    public ResponseEntity<HttpErrorResponse> friendshipException(FriendshipException exception) {
        LOGGER.error(exception.getMessage());
        return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    private ResponseEntity<HttpErrorResponse> createHttpResponse(HttpStatus httpStatus, String message){
        HttpErrorResponse httpErrorResponse = HttpErrorResponse.builder()
                .timeStamp(LocalDateTime.now())
                .httpStatusCode(httpStatus.value())
                .reason(httpStatus.getReasonPhrase().toUpperCase())
                .message(message)
                .build();
        return new ResponseEntity<>(httpErrorResponse, httpStatus);
    }
    @ExceptionHandler(AlreadyReactedException.class)
    public ResponseEntity<String> handleAlreadyReactedException(AlreadyReactedException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.CONFLICT); // 409 Conflict
    }
    @ExceptionHandler(UserBlockedException.class)
    public ResponseEntity<String> handleUserBlocked(UserBlockedException ex) {
        return ResponseEntity.status(403).body(ex.getMessage());
    }

    @ExceptionHandler(UserAlreadyBlockedException.class)
    public ResponseEntity<HttpErrorResponse> handleUserAlreadyBlockedException(UserAlreadyBlockedException exception) {
        return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(UserNotBlockedException.class)
    public ResponseEntity<HttpErrorResponse> handleUserNotBlockedException(UserNotBlockedException exception) {
        return createHttpResponse(HttpStatus.BAD_REQUEST, exception.getMessage());
    }
}
