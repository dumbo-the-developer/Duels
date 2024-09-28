package com.meteordevelopments.duels.config.converters;

import com.meteordevelopments.duels.util.config.convert.Converter;

import java.util.HashMap;
import java.util.Map;

public class ConfigConverter9_10 implements Converter {

    @Override
    public Map<String, String> renamedKeys() {
        final Map<String, String> keys = new HashMap<>();
        keys.put("duel.use-own-inventory.enabled", "request.use-own-inventory.enabled");
        return keys;
    }
}
