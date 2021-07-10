package com.waya.wayaauthenticationservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.util.WebUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.time.ZonedDateTime;
import java.util.*;

@RestControllerAdvice
@Slf4j
public class WayaGlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public final ResponseEntity<Object> handleAllExceptions(Exception ex) {
        String message = ex.getLocalizedMessage();
        log.error(ex.getMessage());
        return buildResponseEntity(message, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    @ExceptionHandler(MissingHeaderInfoException.class)
    public final ResponseEntity<Object> handleInvalidTraceIdException(MissingHeaderInfoException ex, WebRequest request) {
        String message = ex.getLocalizedMessage();
        log.error(ex.getMessage());
        return buildResponseEntity(message, HttpStatus.EXPECTATION_FAILED);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public final ResponseEntity<Object> handleBadCredentialsException(BadCredentialsException ex) {
        String message = ex.getLocalizedMessage();
        log.error(ex.getMessage());
        return buildResponseEntity(message, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<Object> handleMaxSizeException(MaxUploadSizeExceededException exc) {
        String message = "File too Large for Upload. Maximum file Size: " + exc.getMaxUploadSize();
        return buildResponseEntity(message, HttpStatus.EXPECTATION_FAILED);
    }

    /**
     * Handle MethodArgumentNotValidException. Triggered when an object fails @Valid validation.
     *
     * @param ex      the MethodArgumentNotValidException that is thrown when @Valid validation fails
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity(getBodyValidationErrors(ex.getBindingResult().getFieldErrors()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException cve) {
        return buildResponseEntity(getValidationErrors(cve.getConstraintViolations()), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Object> handleResourceNotFoundException(ResourceNotFoundException re) {
        return buildResponseEntity(re.getMessage(), HttpStatus.NOT_FOUND);
    }

    /**
     * Handle HttpMessageNotReadableException. Happens when request JSON is malformed.
     *
     * @param ex      HttpMessageNotReadableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity("Malformed JSON request", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    /**
     * {@inheritDoc}
     */
    @ExceptionHandler(MissingPathVariableException.class)
    protected ResponseEntity<Object> handleMissingPathVariable(
            MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, getError(ex), headers, status, request);
    }

    /**
     * {@inheritDoc}
     */
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(
            HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {

        return handleExceptionInternal(ex, getError(ex), headers, status, request);
    }

    /**
     * {@inheritDoc}
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<Object> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(ex, getError(ex), headers, status, request);
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<Object> handleCustomException(CustomException ce) {
        return buildResponseEntity(ce.getMessage(), ce.getStatus());
    }

    /**
     * Handle HttpMediaTypeNotSupportedException. This one triggers when JSON is invalid as well.
     *
     * @param ex      HttpMediaTypeNotSupportedException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(ex.getContentType());
        builder.append(" media type is not supported. Supported media types are ");
        ex.getSupportedMediaTypes().forEach(t -> builder.append(t).append(", "));
        return buildResponseEntity(builder.substring(0, builder.length() - 2), HttpStatus.UNSUPPORTED_MEDIA_TYPE);
    }

    /**
     * Handle HttpMessageNotWritableException.
     *
     * @param ex      HttpMessageNotWritableException
     * @param headers HttpHeaders
     * @param status  HttpStatus
     * @param request WebRequest
     * @return the ApiError object
     */
    @ExceptionHandler(HttpMessageNotWritableException.class)
    protected ResponseEntity<Object> handleHttpMessageNotWritable(
            HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return buildResponseEntity("Error writing JSON output", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handle NoHandlerFoundException
     *
     * @param ex      Exception Object
     * @param headers Headers
     * @param status  Status
     * @param request Request
     * @return ResponseEntity<Object>
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    protected ResponseEntity<Object> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        String message = String.format("Could not find the %s method for URL %s", ex.getHttpMethod(), ex.getRequestURL());
        return buildResponseEntity(message, HttpStatus.NOT_FOUND);
    }

    private ResponseEntity<Object> buildResponseEntity(String apiResponse, HttpStatus status) {
        return new ResponseEntity<>(getError(apiResponse), status);
    }

    private ResponseEntity<Object> buildResponseEntity(Object apiResponse, HttpStatus status) {
        return new ResponseEntity<>(getError("Validation Errors", apiResponse), status);
    }

    private Map<String, String> getValidationErrors(Set<ConstraintViolation<?>> constraintViolations) {
        Map<String, String> errors = new HashMap<>();
        constraintViolations.forEach(e ->
                errors.put(((PathImpl) e.getPropertyPath()).getLeafNode().asString(), e.getMessage())
        );
        return errors;
    }

    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        if (HttpStatus.INTERNAL_SERVER_ERROR.equals(status)) {
            request.setAttribute(WebUtils.ERROR_EXCEPTION_ATTRIBUTE, ex, WebRequest.SCOPE_REQUEST);
        }
        return new ResponseEntity<>(body, headers, status);
    }

    private List<String> getBodyValidationErrors(List<FieldError> fieldErrors) {
        List<String> errors = new ArrayList<>();
        fieldErrors.forEach(e -> errors.add(e.getDefaultMessage()));
        return errors;
    }

    private Map<String, Object> getError(Exception ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", ex.getMessage());
        response.put("status", false);
        response.put("timeStamp", ZonedDateTime.now());
        response.put("data", null);
        return response;
    }

    private Map<String, Object> getError(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", false);
        response.put("timeStamp", ZonedDateTime.now());
        response.put("data", null);

        return response;
    }

    private Map<String, Object> getError(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", message);
        response.put("status", false);
        response.put("timeStamp", ZonedDateTime.now());
        response.put("data", data);

        return response;
    }
}
