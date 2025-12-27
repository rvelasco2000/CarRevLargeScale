package it.unipi.CarRev.dao.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;

/**
 * Stores refresh tokens in Redis.
 * We store mapping: refresh:<jti> -> username with TTL = refresh token expiration.
 */
@Repository
public class RefreshTokenDAO {

    private final StringRedisTemplate redis;
    private final Duration ttl;

    public RefreshTokenDAO(
            StringRedisTemplate redis,
            @Value("${jwt.refresh.expiration}") long refreshExpSeconds
    ) {
        this.redis = redis;
        this.ttl = Duration.ofSeconds(refreshExpSeconds);
    }

    private String key(String jti) {
        return "refresh:" + jti;
    }

    /** Save refresh token (by jti) for a username. */
    public void store(String jti, String username) {
        redis.opsForValue().set(key(jti), username, ttl);
    }

    /** Return username if refresh token is still valid (exists), otherwise null. */
    public String getUsername(String jti) {
        return redis.opsForValue().get(key(jti));
    }

    /** Revoke refresh token (logout). */
    public void revoke(String jti) {
        redis.delete(key(jti));
    }
}
