package io.github.timestop34.timeindustry.world.registry;

import io.github.timestop34.timeindustry.world.block.Block;

import java.util.HashMap;
import java.util.Map;

public class BlockRegistry {
    private static final Map<String, Block> REGISTRY = new HashMap<>();

    public static void register(String fullId,Block def) {
        if (REGISTRY.containsKey(fullId))
            throw new IllegalArgumentException("Duplicate block id: " + fullId);
        REGISTRY.put(fullId, def);
    }

    public static Block get(String id) {
        return REGISTRY.get(id);
    }

    public static Map<String, Block> getAll() {
        return new HashMap<>(REGISTRY);
    }
}
