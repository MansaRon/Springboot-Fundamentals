package co.za.ecommerce.aspect;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyAspect")
class IdempotencyAspectTest {

    @Mock private ProceedingJoinPoint pjp;
    @Mock private co.za.ecommerce.annotation.Idempotent idempotent;

    private Cache<String, IdempotencyAspect.IdempotencyRecord> idempotencyCache;
    private IdempotencyAspect aspect;

    @BeforeEach
    void setUp() {
        idempotencyCache = Caffeine.newBuilder().build();
        aspect = new IdempotencyAspect(idempotencyCache);
        lenient().when(idempotent.ttlHours()).thenReturn(24L);
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
        @DisplayName("proceeds without touching the cache when header is absent")
        void noHeader_proceedsDirectly() throws Throwable {
            bindRequest(new MockHttpServletRequest());
            when(pjp.proceed()).thenReturn(ResponseEntity.ok("ok"));

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp).proceed();
            assertThat(idempotencyCache.estimatedSize()).isZero();
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("proceeds normally when no request context is bound (e.g. async)")
        void noRequestContext_proceedsDirectly() throws Throwable {
            // RequestContextHolder has nothing bound → resolveHeader() catches IllegalStateException
            when(pjp.proceed()).thenReturn(ResponseEntity.ok("ok"));

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp).proceed();
            assertThat(idempotencyCache.estimatedSize()).isZero();
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
            idempotencyCache.put("idempotency:key-abc",
                    new IdempotencyAspect.IdempotencyRecord(originalBody, 200, Duration.ofHours(24)));

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
        @DisplayName("stores result in the cache when response is 2xx")
        void cacheMiss_2xxResponse_storesInCache() throws Throwable {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Idempotency-Key", "key-xyz");
            bindRequest(request);

            Map<String, Object> body = Map.of("id", "order-1");
            when(pjp.proceed()).thenReturn(ResponseEntity.ok(body));

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp).proceed();
            IdempotencyAspect.IdempotencyRecord stored = idempotencyCache.getIfPresent("idempotency:key-xyz");
            assertThat(stored).isNotNull();
            assertThat(stored.statusCode()).isEqualTo(200);
            assertThat(stored.body()).isEqualTo(body);
            assertThat(stored.ttl()).isEqualTo(Duration.ofHours(24));
            assertThat(result).isInstanceOf(ResponseEntity.class);
        }

        @Test
        @DisplayName("does NOT store result in the cache when response is 4xx")
        void cacheMiss_4xxResponse_doesNotStore() throws Throwable {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Idempotency-Key", "key-bad");
            bindRequest(request);

            when(pjp.proceed()).thenReturn(ResponseEntity.badRequest().build());

            Object result = aspect.enforce(pjp, idempotent);

            verify(pjp).proceed();
            assertThat(idempotencyCache.getIfPresent("idempotency:key-bad")).isNull();
            assertThat(result).isInstanceOf(ResponseEntity.class);
        }

        @Test
        @DisplayName("re-throws exception without storing in the cache")
        void cacheMiss_exceptionThrown_doesNotStore() throws Throwable {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Idempotency-Key", "key-err");
            bindRequest(request);

            when(pjp.proceed()).thenThrow(new RuntimeException("service failure"));

            assertThatThrownBy(() -> aspect.enforce(pjp, idempotent))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("service failure");

            assertThat(idempotencyCache.getIfPresent("idempotency:key-err")).isNull();
        }
    }
}
