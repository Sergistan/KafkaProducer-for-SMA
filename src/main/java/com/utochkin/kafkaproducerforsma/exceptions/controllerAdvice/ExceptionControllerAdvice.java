package com.utochkin.kafkaproducerforsma.exceptions.controllerAdvice;


import com.utochkin.kafkaproducerforsma.dto.response.ErrorResponse;
import com.utochkin.kafkaproducerforsma.exceptions.*;
import org.apache.tomcat.util.http.fileupload.impl.FileSizeLimitExceededException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;



@RestControllerAdvice
public class ExceptionControllerAdvice {
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handlerAccessDeniedException(AccessDeniedException accessDeniedException) {
        return new ErrorResponse(accessDeniedException.getMessage());
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadCredentialsException.class)
    public ErrorResponse handlerErrorBadCredentialsException(BadCredentialsException badCredentialsException) {
        return new ErrorResponse(badCredentialsException.getMessage());
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(UserNotFoundException.class)
    public ErrorResponse handlerUserNotFoundException(UserNotFoundException userNotFoundException) {
        return new ErrorResponse(userNotFoundException.getMessage());
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PostNotFoundException.class)
    public ErrorResponse handlerPostNotFoundException(PostNotFoundException postNotFoundException) {
        return new ErrorResponse(postNotFoundException.getMessage());
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(ChatNotFoundException.class)
    public ErrorResponse handlerChatNotFoundException(ChatNotFoundException chatNotFoundException) {
        return new ErrorResponse(chatNotFoundException.getMessage());
    }
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(MessageNotFoundException.class)
    public ErrorResponse handlerChatNotFoundException(MessageNotFoundException messageNotFoundException) {
        return new ErrorResponse(messageNotFoundException.getMessage());
    }
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(BadInputDataException.class)
    public ErrorResponse handlerBadInputDataException(BadInputDataException badInputDataException) {
        return new ErrorResponse(badInputDataException.getMessage());
    }
    @ResponseStatus(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ErrorResponse handlerFileSizeLimitExceededException(FileSizeLimitExceededException fileSizeLimitExceededException) {
        return new ErrorResponse(fileSizeLimitExceededException.getMessage());
    }

}
