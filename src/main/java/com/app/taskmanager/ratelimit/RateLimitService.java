package com.app.taskmanager.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitService {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key, int capacity, Duration refill) {
        return buckets.computeIfAbsent(key, k -> createNewBucket(capacity, refill));
    }

    private Bucket createNewBucket(int capacity, Duration refill) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, refill)
                .build();

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}