package nescol.connect.service;

import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService extends RedisProperties.Jedis {

    private final RedisTemplate<Object, Object> redisTemplate;

    public RedisService(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void storeWithExpiry(Object key, Object value, long timeoutInSeconds) {
        redisTemplate.opsForValue().set(key, value, timeoutInSeconds, TimeUnit.SECONDS);
    }

    public void extendKeyExpiry(Object key, long newTimeoutInSeconds) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            redisTemplate.expire(key, newTimeoutInSeconds, TimeUnit.SECONDS);
        }
    }

    public Object get(Object key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void remove(Object key) {
        redisTemplate.delete(key);
    }

    public void addToSet(Object key, Object value) {
        BoundSetOperations<Object, Object> setOps = redisTemplate.boundSetOps(key);
        setOps.add(value);
    }

    public void addToSetWithExpiry(Object key, Object value, long expiryTime) {
        BoundSetOperations<Object, Object> setOps = redisTemplate.boundSetOps(key);
        setOps.add(value);
        redisTemplate.expire(key, expiryTime, TimeUnit.SECONDS);
    }


    public Set<Object> getSet(Object key) {
        BoundSetOperations<Object, Object> setOps = redisTemplate.boundSetOps(key);
        return setOps.members();
    }

    public void removeFromSet(Object key, Object value) {
        BoundSetOperations<Object, Object> setOps = redisTemplate.boundSetOps(key);
        setOps.remove(value);
    }

    public void addToHashMapWithExpiry(Object hashMapKey, Object key, Object value, long expiryTime) {
        BoundHashOperations<Object, Object, Object> hashOps = redisTemplate.boundHashOps(hashMapKey);
        hashOps.put(key, value);
        redisTemplate.expire(hashMapKey, expiryTime, TimeUnit.SECONDS);
    }

    public Map<Object, Object> getHashMap(Object key) {
        BoundHashOperations<Object, Object, Object> hashOps = redisTemplate.boundHashOps(key);
        return hashOps.entries();
    }
}
