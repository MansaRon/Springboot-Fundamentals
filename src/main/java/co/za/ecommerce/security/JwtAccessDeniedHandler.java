package co.za.ecommerce.security;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import co.za.ecommerce.utils.DateUtil;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Handles 403 Forbidden errors.
 * Triggered when:
 * - A valid JWT is present but the user lacks the required role
 * - A ROLE_USER tries to access a ROLE_ADMIN endpoint
 * - @PreAuthorize check fails at the filter chain level
 *
 * Returns a GlobalApiErrorResponse consistent with all other
 * exception responses in the application.
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException {
        log.warn("Access denied to: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        GlobalApiErrorResponse errorResponse = GlobalApiErrorResponse.builder()
                .status(HttpStatus.FORBIDDEN.toString())
                .statusCode(HttpStatus.FORBIDDEN.value())
                .message("Access denied. You do not have permission to access this resource.")
                .path(request.getRequestURI())
                .timestamp(DateUtil.now())
                .build();

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
