package com.profileMangager.profile.service;

import com.profileMangager.profile.entity.Profile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class ProfileRedisCache {

    private static final Logger log = LoggerFactory.getLogger(ProfileRedisCache.class);

    private final StringRedisTemplate redisTemplate;

    private final HashOperations<String, String, String> hashOps;

    public ProfileRedisCache(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOps = redisTemplate.opsForHash();
    }
// khi nào cần dùng ttl và lru
    // có thể kết hợp cả 2

    private String key(long id) {
        return "profile:" + id;
    }

    public void save(Profile profile) {
        String k = key(profile.getUserId());


        Map<String, String> hashMap = new HashMap<>();
        hashMap.put("username", profile.getUsername() != null ? profile.getUsername() : "");
        hashMap.put("gender", String.valueOf(profile.isGender()));
        hashMap.put("age", String.valueOf(profile.getAge()));


        hashOps.putAll(k, hashMap);
        redisTemplate.expire(k, Duration.ofMinutes(30));

        log.info("[REDIS] Saved profile id={} vào Redis", profile.getUserId());
    }

    public Profile get(long id) {
        String k = key(id);
        Map<String, String> map = hashOps.entries(k);

        if(map == null || map.isEmpty()) {
            log.info("[REDIS] Profile not found for id={}", id);
            return null;
        }

        // ← SỬA: Validate required field: username
        String username = map.get("username");
        if(username == null || username.isEmpty()) {
            log.warn("[REDIS] Profile id={} missing username, treating as cache miss", id);
            return null; // Cache corrupted, treat as miss
        }


        Profile p = new Profile();
        p.setUserId(id);
        p.setUsername(username);
        p.setGender(Boolean.parseBoolean(map.getOrDefault("gender", "false")));
        p.setAge(Integer.parseInt(map.getOrDefault("age", "0")));

        log.info("[REDIS] HIT profile id={} trong Redis", id);
        return p;
    }

    public void delete(long id) {
        String k = key(id);
        redisTemplate.delete(k);
        log.info("[REDIS] Deleted profile id={} trong Redis", id);
    }
}