package io.github.vab2048.axon.exhibition.app.controller;

import io.github.vab2048.axon.exhibition.app.controller.dto.ControllerDTOs.InternalServerErrorResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;
import java.util.*;

/**
 * "Last resort" exception handlers to be invoked when an exception is thrown from any
 * controller mapping.
 *
 * These are the "last resort" exception handlers, in the sense that they will only be run if a
 * more specific exception handler has not been defined (on a @Controller itself, for example).
 */
@ControllerAdvice
public class GlobalControllerExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalControllerExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<InternalServerErrorResponseBody> globalExceptionHandler(Exception ex, WebRequest request) {
        log.error("""
                \s
                ERROR IN PROCESSING WEB REQUEST. RESPONDING WITH INTERNAL SERVER ERROR (500).
                ============
                Exception Type:       {}
                Exception Message:    {}
                Request Description:  {}
                Request Parameters:   {}
                Request Headers:      {}
                """, ex.getClass(), ex.getMessage(), request, generateRequestParametersSummaryString(request),
                generateRequestHeadersSummaryString(request));

        return ResponseEntity.internalServerError()
                .body(new InternalServerErrorResponseBody(Instant.now(), ex.getMessage(),
                        request.getDescription(true)));
    }

    private String generateRequestParametersSummaryString(WebRequest request) {
        Map<String, String[]> parameterMap = request.getParameterMap();
        List<String> parameters = parameterMap.entrySet().stream()
                .map(entry -> entry.getKey() + " : " + Arrays.toString(entry.getValue()))
                .toList();
        return parameters.isEmpty() ? "NONE"
                : "[\n    " + String.join(",\n    ", parameters) + "\n]";
    }

    private String generateRequestHeadersSummaryString(WebRequest request) {
        List<String> headers = new ArrayList<>();
        Iterator<String> headerNames = request.getHeaderNames();
        while(headerNames.hasNext()) {
            String headerName = headerNames.next();
            headers.add(headerName + " : " + Arrays.toString(request.getHeaderValues(headerName)));
        }
        return headers.isEmpty() ? "NONE"
                : "[\n    " + String.join(",\n    ", headers) + "\n]";
    }

}
