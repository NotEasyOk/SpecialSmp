package com.noteasyok.spcialsmp.manager;

import java.util.HashMap;
import java.util.UUID;

public class CooldownManager {

    private final HashMap<String, Long> map = new HashMap<>();

    public boolean onCooldown(UUID uuid, String key) {
        String k = uuid + ":" + key;
        if (!map.containsKey(k)) return false;
        return map.get(k) > System.currentTimeMillis();
    }

    public long timeLeft(UUID uuid, String key) {
        return (map.get(uuid + ":" + key) - System.currentTimeMillis()) / 1000;
    }

    public void set(UUID uuid, String key, int sec) {
        map.put(uuid + ":" + key, System.currentTimeMillis() + sec * 1000L);
    }
}
