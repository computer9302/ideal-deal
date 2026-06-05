package com.auction.bid.global.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RedisConfigTest {

    @Autowired
    @Qualifier("cartRedisTemplate")
    private RedisTemplate<String, Object> cartRedisTemplate;

    @Test
    void cartRedisTemplate_setAndGet() {
        assertNotNull(cartRedisTemplate);
    }
}
