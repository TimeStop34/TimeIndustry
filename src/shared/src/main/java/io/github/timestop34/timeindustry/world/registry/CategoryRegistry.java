package io.github.timestop34.timeindustry.world.registry;

import io.github.timestop34.timeindustry.world.block.Category;

import java.util.HashMap;
import java.util.Map;

public class CategoryRegistry {
    private static final Map<String, Category> REGISTRY = new HashMap<>();

    public static void register(Category category) {
        if (REGISTRY.containsKey(category.id()))
            throw new IllegalArgumentException("Duplicate category id: " + category.id());
        REGISTRY.put(category.id(), category);
    }

    public static Category get(String id) {
        return REGISTRY.get(id);
    }

    public static Map<String, Category> getAll() {
        return new HashMap<>(REGISTRY);
    }
}