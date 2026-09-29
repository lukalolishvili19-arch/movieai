package com.movieai.security;

import java.io.IOException;

import jakarta.servlet.http.HttpServletResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.movieai.exception.ErrorCode;
import com.movieai.exception.ErrorResponse;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

/** Writes the uniform error envelope from filters, outside of Spring MVC's exception handling. */
@Component
public class JsonErrorWriter {

    private final ObjectMapper objectMapper;

    public JsonErrorWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void write(HttpServletResponse response, ErrorCode code) throws IOException {
        response.setStatus(code.status().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        objectMapper.writeValue(response.getOutputStream(), ErrorResponse.of(code, code.defaultMessage()));
    }
}
