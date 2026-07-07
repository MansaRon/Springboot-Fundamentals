package co.za.ecommerce.exception;

import co.za.ecommerce.dto.GlobalApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.UUID;
import java.util.stream.Collectors;

import static co.za.ecommerce.utils.DateUtil.now;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final String CORRELATION_HEADER = "X-Correlation-ID";
    private static final String MDC_KEY = "correlationId";

    // ── Domain exceptions ────────────────────────────────────────────────────

    @ExceptionHandler(ClientException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleClientException(
            final ClientException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] ClientException: {}", cid, ex.getMessage());
        return build(ex.getCode().value(), ex.getCode().toString(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(OTPException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleOTPException(
            final OTPException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] OTPException: {}", cid, ex.getMessage());
        return build(ex.getCode().value(), ex.getCode().toString(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(ProductException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleProductException(
            final ProductException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] ProductException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(CartException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleCartException(
            final CartException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] CartException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(WishlistException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleWishlistException(
            final WishlistException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] WishlistException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(CheckoutException.class)
    public ResponseEntity<GlobalApiErrorResponse> checkoutException(
            final CheckoutException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] CheckoutException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(OrderException.class)
    public ResponseEntity<GlobalApiErrorResponse> orderException(
            final OrderException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] OrderException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(PaymentException.class)
    public ResponseEntity<GlobalApiErrorResponse> handlePaymentException(
            final PaymentException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.error("[{}] PaymentException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(InventoryException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleInventoryException(
            final InventoryException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] InventoryException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(MethodNotAllowedException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleMethodNotAllowedException(
            final MethodNotAllowedException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] MethodNotAllowedException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<GlobalApiErrorResponse> resourceNotFoundExceptionException(
            final ResourceNotFoundException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] ResourceNotFoundException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<GlobalApiErrorResponse> userNotFoundExceptionException(
            final UserNotFoundException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] UserNotFoundException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<GlobalApiErrorResponse> validationException(
            final ValidationException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] ValidationException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getCode(), ex.getMessage(), path(request), cid);
    }

    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<GlobalApiErrorResponse> nullPointerException(
            final NullPointerException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.error("[{}] NullPointerException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getMessage(), "Null pointer exception occurred", path(request), cid);
    }

    @ExceptionHandler(ArrayIndexOutOfBoundsException.class)
    public ResponseEntity<GlobalApiErrorResponse> arrayIndexOutOfBoundsException(
            final ArrayIndexOutOfBoundsException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.error("[{}] ArrayIndexOutOfBoundsException: {}", cid, ex.getMessage());
        return build(ex.getStatus(), ex.getMessage(), "Array out of bounds has occurred", path(request), cid);
    }

    // ── Security exceptions ──────────────────────────────────────────────────

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleAccessDeniedException(
            final AccessDeniedException ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] AccessDeniedException: {}", cid, ex.getMessage());
        return build(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN.toString(),
                "Access denied. You do not have permission to access this resource.", path(request), cid);
    }

    @ExceptionHandler(org.springframework.security.core.AuthenticationException.class)
    public ResponseEntity<GlobalApiErrorResponse> handleAuthenticationException(
            final org.springframework.security.core.AuthenticationException ex,
            final HttpServletRequest request) {
        String cid = cid(request);
        log.warn("[{}] AuthenticationException: {}", cid, ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED.toString(),
                "Authentication required. Please login to access this resource.", path(request), cid);
    }

    // ── Spring MVC framework exceptions (override parent) ───────────────────

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        HttpServletRequest httpReq = ((ServletWebRequest) request).getRequest();
        String cid = cid(httpReq);
        log.warn("[{}] MethodArgumentNotValid: {}", cid, message);
        GlobalApiErrorResponse body = errorBody(status.value(), HttpStatus.BAD_REQUEST.toString(), message, path(httpReq));
        return ResponseEntity.status(status).header(CORRELATION_HEADER, cid).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(
            org.springframework.web.HttpRequestMethodNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = "HTTP method " + ex.getMethod() + " is not supported for this endpoint. "
                + "Supported methods: " + ex.getSupportedHttpMethods();
        HttpServletRequest httpReq = ((ServletWebRequest) request).getRequest();
        String cid = cid(httpReq);
        log.warn("[{}] MethodNotSupported: {}", cid, message);
        GlobalApiErrorResponse body = errorBody(status.value(), HttpStatus.METHOD_NOT_ALLOWED.toString(), message, path(httpReq));
        return ResponseEntity.status(status).header(CORRELATION_HEADER, cid).body(body);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(
            org.springframework.web.HttpMediaTypeNotSupportedException ex,
            HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        String message = "Unsupported media type: " + ex.getContentType()
                + ". Supported types: " + ex.getSupportedMediaTypes();
        HttpServletRequest httpReq = ((ServletWebRequest) request).getRequest();
        String cid = cid(httpReq);
        log.warn("[{}] MediaTypeNotSupported: {}", cid, message);
        GlobalApiErrorResponse body = errorBody(status.value(), HttpStatus.UNSUPPORTED_MEDIA_TYPE.toString(), message, path(httpReq));
        return ResponseEntity.status(status).header(CORRELATION_HEADER, cid).body(body);
    }

    // ── Catch-all ────────────────────────────────────────────────────────────

    @ExceptionHandler(Exception.class)
    public ResponseEntity<GlobalApiErrorResponse> handleUnexpectedException(
            final Exception ex, final HttpServletRequest request) {
        String cid = cid(request);
        log.error("[{}] Unhandled exception: {}", cid, ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.toString(),
                "An unexpected error occurred. Please try again later.", path(request), cid);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private String cid(HttpServletRequest request) {
        String id = request.getHeader(CORRELATION_HEADER);
        if (id == null || id.isBlank()) id = UUID.randomUUID().toString();
        MDC.put(MDC_KEY, id);
        return id;
    }

    private ResponseEntity<GlobalApiErrorResponse> build(
            int httpStatus, String code, String message, String path, String correlationId) {
        MDC.remove(MDC_KEY);
        return ResponseEntity
                .status(httpStatus)
                .header(CORRELATION_HEADER, correlationId)
                .body(errorBody(httpStatus, code, message, path));
    }

    private GlobalApiErrorResponse errorBody(int httpStatus, String code, String message, String path) {
        return GlobalApiErrorResponse.builder()
                .path(path)
                .status(code)
                .statusCode(httpStatus)
                .message(message)
                .timestamp(now())
                .build();
    }

    private String path(HttpServletRequest request) {
        String path = (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);
        return (path != null && !path.isBlank()) ? path : request.getRequestURI();
    }
}
