package io.github.timestop34.timeindustry.mod;

import io.github.timestop34.timeindustry.world.block.Block;
import io.github.timestop34.timeindustry.world.block.Category;
import io.github.timestop34.timeindustry.world.layers.LayerProperties;
import io.github.timestop34.timeindustry.world.registry.BlockRegistry;
import io.github.timestop34.timeindustry.world.registry.CategoryRegistry;
import io.github.timestop34.timeindustry.world.registry.LayerRegistry;

public class ModHelper {
    public final String modId;
    public final BlocksSubRegistry blocks;
    public final CategoriesSubRegistry categories;
    public final LayersSubRegistry layers;

    public ModHelper(String modId) {
        this.modId = modId;
        this.blocks = new BlocksSubRegistry(modId);
        this.categories = new CategoriesSubRegistry(modId);
        this.layers = new LayersSubRegistry(modId);
    }

    // --- Blocks ---
    public static class BlocksSubRegistry {
        private final String modId;
        public BlocksSubRegistry(String modId) { this.modId = modId; }

        public void register(String localId, Block block) {
            String fullId = modId + ":" + localId;
            BlockRegistry.register(fullId, block);
        }
    }

    // --- Categories ---
    public static class CategoriesSubRegistry {
        private final String modId;
        public CategoriesSubRegistry(String modId) { this.modId = modId; }

        public Category register(String localId, String name, String iconPath) {
            String fullId = modId + ":" + localId;
            Category cat = new Category(fullId, name, iconPath);
            CategoryRegistry.register(cat);
            return cat;
        }
    }

    // --- Layers ---
    public static class LayersSubRegistry {
        private final String modId;

        public LayersSubRegistry(String modId) { this.modId = modId; }

        public LayerProperties register(String localId, String name, String iconPath, boolean unbreakable, boolean unbuildable) {
            String fullId = modId + ":" + localId;
            LayerProperties prop = new LayerProperties(fullId, name, iconPath, unbreakable, unbuildable);
            LayerRegistry.addProperties(prop);   // <-- добавлено
            return prop;
        }
    }
}