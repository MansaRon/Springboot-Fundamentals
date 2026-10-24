package co.za.ecommerce.aspect;

import co.za.ecommerce.annotation.Idempotent;
import com.github.benmanes.caffeine.cache.Cache;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private static final String KEY_PREFIX = "idempotency:";

    private final Cache<String, IdempotencyRecord> idempotencyCache;

    @Around("@annotation(idempotent)")
    public Object enforce(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        String idempotencyKey = resolveHeader();
        if (idempotencyKey == null) {
            return pjp.proceed();
        }

        String cacheKey = KEY_PREFIX + idempotencyKey;

        IdempotencyRecord cached = idempotencyCache.getIfPresent(cacheKey);
        if (cached != null) {
            log.debug("Idempotency hit for key={}", idempotencyKey);
            return ResponseEntity.status(cached.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(cached.body());
        }

        Object result = pjp.proceed();

        if (result instanceof ResponseEntity<?> response && response.getStatusCode().is2xxSuccessful()) {
            idempotencyCache.put(cacheKey, new IdempotencyRecord(
                    response.getBody(),
                    response.getStatusCode().value(),
                    Duration.ofHours(idempotent.ttlHours())));
            log.debug("Idempotency stored for key={}, ttl={}h", idempotencyKey, idempotent.ttlHours());
        }

        return result;
    }

    private String resolveHeader() {
        try {
            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attrs.getRequest();
            return request.getHeader("Idempotency-Key");
        } catch (IllegalStateException e) {
            return null;
        }
    }

    /**
     * A cached response. {@code ttl} is the entry's time-to-live, applied per-entry by the
     * idempotency cache's {@link com.github.benmanes.caffeine.cache.Expiry} policy.
     */
    public record IdempotencyRecord(Object body, int statusCode, Duration ttl) {
    }
}
