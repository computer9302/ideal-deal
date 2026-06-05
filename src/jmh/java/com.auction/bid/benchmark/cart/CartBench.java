package com.auction.bid.benchmark.cart;

import com.auction.bid.domain.localCart.LocalCartRepository;
import com.auction.bid.domain.redisCart.CartItem;
import com.auction.bid.domain.redisCart.CartRepository;
import net.bytebuddy.agent.builder.AgentBuilder;
import org.openjdk.jmh.annotations.*;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
@State(Scope.Benchmark)
public class CartBench {

    private LocalCartRepository local;
    private com.auction.bid.domain.redisCart.CartRepository redis;
    private LettuceConnectionFactory cf; // tearDown.에서 쓰려면 필드로

    String userId = "user123";
    List<String> products = Arrays.asList("A", "B", "C", "D", "E");

    @Setup(Level.Trial)
    public void setup(){

        // 1) Local
        local = new LocalCartRepository();
        local.clear(userId);

        // 2) Redis(수동으로 스프링 역할)
        cf = new LettuceConnectionFactory("localhost", 6379);
        cf.afterPropertiesSet();

        RedisTemplate<String, Object> tp1 = new RedisTemplate<>();
        tp1.setConnectionFactory(cf);

        // 직렬화: 프로덕션과 동일하게!
        tp1.setKeySerializer(new StringRedisSerializer());
        tp1.setHashKeySerializer(new StringRedisSerializer());
        tp1.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        tp1.afterPropertiesSet();

        redis = new com.auction.bid.domain.redisCart.CartRepository(tp1);
        redis.clearCart(userId);

    }

    @Benchmark
    public Map<String, Integer> local_add_get(){
        local.updateQuantity(userId, pick(), 1); //  수량1 증가(업서트)
        return local.findAll(userId);                   // 조회
    }

    @Benchmark
    public Map<String, Integer> local_get_only(){
        return local.findAll(userId);       // 조회만
    }

    @Benchmark
    public List<CartItem> redis_get(){
        return redis.getCart(userId);
    }

    @Benchmark
    public List<CartItem> redis_add_get(){
        redis.addToCart(userId, CartItem.builder()
                .productId("prodA").name("A").quantity(1).price(1000).build());
        return redis.getCart(userId);
    }

    private String pick(){
        return "prod" + products.get((int)(Math.random()* products.size()));
    }
}
