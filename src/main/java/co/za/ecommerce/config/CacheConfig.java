package co.za.ecommerce.config;

import co.za.ecommerce.aspect.IdempotencyAspect.IdempotencyRecord;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * In-memory caching backed by <a href="https://github.com/ben-manes/caffeine">Caffeine</a>.
 *
 * <p>Each cache is bounded (both by size and by time) so the heap can never grow
 * unbounded. Caches are registered explicitly with {@link SimpleCacheManager}, so a
 * typo in a {@code @Cacheable} name fails fast instead of silently creating an
 * unconfigured cache.
 */
@Configuration
public class CacheConfig {

    /**
     * Per-cache specification: name, time-to-live after write, and a bound on the
     * number of entries kept in memory.
     */
    private record CacheSpec(String name, Duration ttl, long maximumSize) {
    }

    private static final List<CacheSpec> CACHE_SPECS = List.of(
            new CacheSpec("products",        Duration.ofMinutes(10), 1_000),
            new CacheSpec("product-list",    Duration.ofMinutes(5),  500),
            new CacheSpec("product-reviews", Duration.ofMinutes(10), 1_000),
            new CacheSpec("carts",           Duration.ofMinutes(2),  1_000),
            new CacheSpec("orders",          Duration.ofMinutes(30), 1_000),
            new CacheSpec("user-orders",     Duration.ofMinutes(5),  1_000),
            new CacheSpec("wishlist",        Duration.ofMinutes(5),  1_000)
    );

    @Bean
    public CacheManager cacheManager() {
        List<CaffeineCache> caches = CACHE_SPECS.stream()
                .map(spec -> new CaffeineCache(spec.name(), Caffeine.newBuilder()
                        .expireAfterWrite(spec.ttl())
                        .maximumSize(spec.maximumSize())
                        .build()))
                .toList();

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(caches);
        return cacheManager;
    }

    /**
     * Dedicated store for HTTP idempotency records. Unlike the Spring-managed caches
     * above, each entry carries its own time-to-live (driven by {@code @Idempotent#ttlHours})
     * so it is expired via a per-entry {@link Expiry} rather than a single fixed TTL.
     */
    @Bean
    public Cache<String, IdempotencyRecord> idempotencyCache() {
        return Caffeine.newBuilder()
                .maximumSize(10_000)
                .expireAfter(new Expiry<String, IdempotencyRecord>() {
                    @Override
                    public long expireAfterCreate(String key, IdempotencyRecord value, long currentTime) {
                        return value.ttl().toNanos();
                    }

                    @Override
                    public long expireAfterUpdate(String key, IdempotencyRecord value,
                                                  long currentTime, long currentDuration) {
                        return value.ttl().toNanos();
                    }

                    @Override
                    public long expireAfterRead(String key, IdempotencyRecord value,
                                                long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                })
                .build();
    }
}
