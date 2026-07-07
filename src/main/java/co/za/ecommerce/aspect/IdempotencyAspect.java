package co.za.ecommerce.aspect;

import co.za.ecommerce.annotation.Idempotent;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Duration;
import java.util.Map;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class IdempotencyAspect {

    private static final String KEY_PREFIX = "idempotency:";

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Around("@annotation(idempotent)")
    public Object enforce(ProceedingJoinPoint pjp, Idempotent idempotent) throws Throwable {
        String idempotencyKey = resolveHeader();
        if (idempotencyKey == null) {
            return pjp.proceed();
        }

        String redisKey = KEY_PREFIX + idempotencyKey;

        String cached = redisTemplate.opsForValue().get(redisKey);
        if (cached != null) {
            log.debug("Idempotency hit for key={}", idempotencyKey);
            IdempotencyRecord record = objectMapper.readValue(cached, IdempotencyRecord.class);
            Map<String, Object> body = objectMapper.readValue(
                    record.body(), new TypeReference<>() {});
            return ResponseEntity.status(record.statusCode())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        }

        Object result = pjp.proceed();

        if (result instanceof ResponseEntity<?> response && response.getStatusCode().is2xxSuccessful()) {
            String bodyJson = objectMapper.writeValueAsString(response.getBody());
            String payload = objectMapper.writeValueAsString(
                    new IdempotencyRecord(bodyJson, response.getStatusCode().value()));
            redisTemplate.opsForValue().set(redisKey, payload, Duration.ofHours(idempotent.ttlHours()));
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

    record IdempotencyRecord(String body, int statusCode) {}
}
