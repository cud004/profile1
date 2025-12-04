package com.profileMangager.profile.service;



import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache<K, V>  {
    private final int maxSize;// toi da bao nhieu phan tu
    private final LinkedHashMap<K, V> map;
    public LRUCache(int maxSize) {
        this.maxSize = maxSize;
        this.map = new LinkedHashMap<>(maxSize, 0.75f, true){
            @Override
            protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                return size() > LRUCache.this.maxSize;
            }
        };
    }
    public synchronized V get(K key) {
        return map.get(key);
    }
    public synchronized void put(K key, V value) {
        map.put(key, value);
    }
    public synchronized void remove(K key) {
        map.remove(key);
    }
    public synchronized void clear() {
        map.clear();
    }

    public synchronized int size() {
        return map.size();
    }





}
