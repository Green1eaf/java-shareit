package ru.practicum.shareit.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class CustomExceptionHandler {
    private static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";

    @ExceptionHandler({IllegalArgumentException.class,
            MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseError badRequestHandle(Exception exception) {
        log.error(exception.getMessage());
        return new ResponseError(exception.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseError internalServerErrorHandle(Throwable exception) {
        log.error(exception.getMessage());
        return new ResponseError(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Getter
    @RequiredArgsConstructor
    private static class ResponseError {
        private final String error;
        private final HttpStatus status;
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
        private final LocalDateTime time = LocalDateTime.now();
    }
}
