package co.za.ecommerce.exception;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;

import java.util.HashSet;

public enum ErrorCode {

    UNEXPECTED(5000, HttpStatus.INTERNAL_SERVER_ERROR),
    BINDING_FAIL(4001, HttpStatus.BAD_REQUEST),
    VALIDATION_FAIL(4002, HttpStatus.BAD_REQUEST),
    READ_ONLY_MODE_ENABLE(4003, HttpStatus.METHOD_NOT_ALLOWED),
    REBALANCE_IN_PROGRESS(4004, HttpStatus.CONFLICT),
    DUPLICATED_RECORD(4005, HttpStatus.CONFLICT),
    UNPROCESSABLE_RECORD(4006, HttpStatus.UNPROCESSABLE_ENTITY),
    CLUSTER_NOT_FOUND(4007, HttpStatus.NOT_FOUND),
    TOPIC_NOT_FOUND(4008, HttpStatus.NOT_FOUND),
    OPERATION_FAIL(4009, HttpStatus.BAD_REQUEST),
    SCHEMA_NOT_FOUND(4010, HttpStatus.NOT_FOUND),
    NOT_UNAUTHORIZED(4011, HttpStatus.UNAUTHORIZED),
    CONNECT_NOT_FOUND(4012, HttpStatus.NOT_FOUND);

    static {
        // codes uniqueness check
        var codes = new HashSet<Integer>();
        for (ErrorCode value : ErrorCode.values()) {
            if (!codes.add(value.code())) {
                LoggerFactory.getLogger(ErrorCode.class)
                        .warn("Multiple {} values refer to code {}", ErrorCode.class, value.code);
            }
        }
    }

    private final int code;

    private final HttpStatus httpStatus;

    ErrorCode(int code, HttpStatus httpStatus) {
        this.code = code;
        this.httpStatus = httpStatus;
    }

    public int code() { return code; }

    public HttpStatus httpStatus() { return httpStatus; }
}
