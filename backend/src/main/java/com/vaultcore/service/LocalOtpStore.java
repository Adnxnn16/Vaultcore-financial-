package com.vaultcore.service;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class LocalOtpStore {
    private final Map<String, String> store = new ConcurrentHashMap<>();
    private final Map<String, Long> attempts = new ConcurrentHashMap<>();
    
    public void set(String key, String value) {
        store.put(key, value);
    }
    
    public String get(String key) {
        return store.get(key);
    }
    
    public void delete(String key) {
        store.remove(key);
    }
    
    public Long increment(String key) {
        return attempts.merge(key, 1L, Long::sum);
    }
    
    public void deleteAttempts(String key) {
        attempts.remove(key);
    }
}
