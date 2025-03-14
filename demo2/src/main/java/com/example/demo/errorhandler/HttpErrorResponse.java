package com.example.demo.errorhandler;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HttpErrorResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING,
    pattern = "MM-dd-yyy hh:mm:ss",
    timezone = "Europe/Bucharest")
    private LocalDateTime timeStamp;
    private Integer httpStatusCode;
    private String reason;
    private String message;
}
