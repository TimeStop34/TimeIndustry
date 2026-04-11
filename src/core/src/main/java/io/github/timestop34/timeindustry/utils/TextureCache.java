package io.github.timestop34.timeindustry.utils;

import com.badlogic.gdx.graphics.Texture;
import java.util.HashMap;
import java.util.Map;

public class TextureCache {
    private static final Map<String, Texture> textures = new HashMap<>();

    public static Texture getTexture(String path) {
        if (!textures.containsKey(path)) {
            textures.put(path, new Texture(path));
        }
        return textures.get(path);
    }

    public static void dispose() {
        for (Texture tex : textures.values()) tex.dispose();
        textures.clear();
    }
}