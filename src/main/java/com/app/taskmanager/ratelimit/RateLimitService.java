package com.app.taskmanager.ratelimit;

import com.app.taskmanager.enums.BucketWindow;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
public class RateLimitService {

    private final Cache<String, Bucket> shortBuckets = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterAccess(2, TimeUnit.MINUTES)
            .build();

    private final Cache<String, Bucket> longBuckets = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterAccess(20, TimeUnit.MINUTES)
            .build();

    public Bucket resolveBucket(String key, int capacity, Duration refill, BucketWindow window) {
        Cache<String, Bucket> cache = window == BucketWindow.LONG ? longBuckets : shortBuckets;
        return cache.get(key, k -> createNewBucket(capacity, refill));
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