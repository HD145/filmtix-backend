package com.driver.bookMyShow.Services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate redisTemplate;

    public <T> T get(String key, Class<T> dtoClas){
        try{
            Object obj = redisTemplate.opsForValue().get(key);
            ObjectMapper objM = new ObjectMapper();
            return objM.readValue(obj.toString(), dtoClas);
        }catch(Exception e){
            return null;
        }
    }

    public void set(String key, Object obj, Long ttl){
        try {
            ObjectMapper objM = new ObjectMapper();
            String json = objM.writeValueAsString(obj);
            redisTemplate.opsForValue().set(key, json, ttl, TimeUnit.SECONDS);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializing object to JSON", e);
        }
    }
}
