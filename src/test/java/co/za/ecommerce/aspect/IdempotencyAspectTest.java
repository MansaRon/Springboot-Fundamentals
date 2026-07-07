package co.za.ecommerce.aspect;

import co.za.ecommerce.annotation.Idempotent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyAspect")
class IdempotencyAspectTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOps;
    @Mock private ProceedingJoinPoint pjp;
    @Mock private Idempotent idempotent;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private IdempotencyAspect aspect;

    @BeforeEach
    void setUp() {
        aspect = new IdempotencyAspect(redisTemplate, objectMapper);
        lenient().when(redisTemplate.opsForValue()).thenReturn(valueOps);
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    private void bindRequest(MockHttpServletRequest request) {
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Nested
    @DisplayName("No Idempotency-Key header")
    class NoHeader {

        @Test
        @DisplayName("proceeds without touching Redis when header is absent")
        void noHeader_proceedsDirectly() throws Throwable {
            bindRequest(new MockHttpServletRequest());
            when(pjp.proceed()).thenReturn(ResponseEntity.ok("ok"));

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp).proceed();
            verify(valueOps, never()).get(anyString());
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("proceeds normally when no request context is bound (e.g. async)")
        void noRequestContext_proceedsDirectly() throws Throwable {
            // RequestContextHolder has nothing bound → resolveHeader() catches IllegalStateException
            when(pjp.proceed()).thenReturn(ResponseEntity.ok("ok"));

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp).proceed();
            verify(valueOps, never()).get(anyString());
            assertThat(result).isNotNull();
        }
    }

    @Nested
    @DisplayName("Cache hit")
    class CacheHit {

        @Test
        @DisplayName("returns the cached response without calling the target method")
        void cacheHit_returnsCachedResponseEntity() throws Throwable {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Idempotency-Key", "key-abc");
            bindRequest(request);

            Map<String, Object> originalBody = Map.of("orderId", "123", "status", "CONFIRMED");
            String bodyJson = objectMapper.writeValueAsString(originalBody);
            IdempotencyAspect.IdempotencyRecord record = new IdempotencyAspect.IdempotencyRecord(bodyJson, 200);
            String cachedPayload = objectMapper.writeValueAsString(record);

            when(valueOps.get("idempotency:key-abc")).thenReturn(cachedPayload);

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp, never()).proceed();
            assertThat(result).isInstanceOf(ResponseEntity.class);
            @SuppressWarnings("unchecked")
            ResponseEntity<Map<String, Object>> response = (ResponseEntity<Map<String, Object>>) result;
            assertThat(response.getStatusCode().value()).isEqualTo(200);
            assertThat(response.getBody()).containsKey("orderId");
        }
    }

    @Nested
    @DisplayName("Cache miss")
    class CacheMiss {

        @Test
        @DisplayName("stores result in Redis when response is 2xx")
        void cacheMiss_2xxResponse_storesInRedis() throws Throwable {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Idempotency-Key", "key-xyz");
            bindRequest(request);

            when(valueOps.get("idempotency:key-xyz")).thenReturn(null);
            when(idempotent.ttlHours()).thenReturn(24L);
            when(pjp.proceed()).thenReturn(ResponseEntity.ok(Map.of("id", "order-1")));

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp).proceed();
            verify(valueOps).set(eq("idempotency:key-xyz"), anyString(), eq(Duration.ofHours(24)));
            assertThat(result).isInstanceOf(ResponseEntity.class);
        }

        @Test
        @DisplayName("does NOT store result in Redis when response is 4xx")
        void cacheMiss_4xxResponse_doesNotStore() throws Throwable {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Idempotency-Key", "key-bad");
            bindRequest(request);

            when(valueOps.get("idempotency:key-bad")).thenReturn(null);
            when(pjp.proceed()).thenReturn(ResponseEntity.badRequest().build());

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp).proceed();
            verify(valueOps, never()).set(any(), any(), any(Duration.class));
            assertThat(result).isInstanceOf(ResponseEntity.class);
        }

        @Test
        @DisplayName("re-throws exception without storing in Redis")
        void cacheMiss_exceptionThrown_doesNotStore() throws Throwable {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Idempotency-Key", "key-err");
            bindRequest(request);

            when(valueOps.get("idempotency:key-err")).thenReturn(null);
            when(pjp.proceed()).thenThrow(new RuntimeException("service failure"));

            assertThatThrownBy(() -> aspect.enforce(pjp, idempotent))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("service failure");

            verify(valueOps, never()).set(any(), any(), any(Duration.class));
        }
    }
}
