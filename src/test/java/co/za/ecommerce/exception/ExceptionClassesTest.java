package co.za.ecommerce.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Exception classes Tests")
class ExceptionClassesTest {

    @Test
    @DisplayName("ResourceNotFoundException should store code, message and status")
    void resourceNotFoundExceptionShouldStoreFields() {
        ResourceNotFoundException ex = new ResourceNotFoundException("NOT_FOUND", "Resource missing", 404);

        assertThat(ex.getMessage()).isEqualTo("Resource missing");
        assertThat(ex.getCode()).isEqualTo("NOT_FOUND");
        assertThat(ex.getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("MethodNotAllowedException should store code, message and status")
    void methodNotAllowedExceptionShouldStoreFields() {
        MethodNotAllowedException ex = new MethodNotAllowedException("METHOD_NOT_ALLOWED", "Not allowed", 405);

        assertThat(ex.getMessage()).isEqualTo("Not allowed");
        assertThat(ex.getCode()).isEqualTo("METHOD_NOT_ALLOWED");
        assertThat(ex.getStatus()).isEqualTo(405);
    }
}
