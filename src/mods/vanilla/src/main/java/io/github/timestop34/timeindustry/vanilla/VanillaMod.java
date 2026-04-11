package io.github.timestop34.timeindustry.vanilla;

import io.github.timestop34.timeindustry.mod.Mod;
import io.github.timestop34.timeindustry.mod.ModHelper;
import io.github.timestop34.timeindustry.mod.ModInitializer;
import io.github.timestop34.timeindustry.world.block.Block;
import io.github.timestop34.timeindustry.world.block.Category;
import io.github.timestop34.timeindustry.world.layers.LayerProperties;

@Mod(modId = "vanilla", name = "TimeIndustry Vanilla")
public class VanillaMod implements ModInitializer {
    @Override
    public void init(ModHelper ctx) {
        // Слои
        LayerProperties ground = ctx.layers.register("ground", "Ground", "rsrc/vanilla/textures/ui/layer_ground.png", true, false);
        LayerProperties buildings = ctx.layers.register("buildings", "Buildings", "rsrc/vanilla/textures/ui/layer_buildings.png", false, false)
                .above("vanilla:ground");

        // Категории
        Category blocksCat = ctx.categories.register("blocks", "Blocks", "rsrc/vanilla/textures/ui/category_blocks.png");

        // Блоки
        Block stone = Block.create("vanilla:stone",
                new Block.BlockProperties()
                        .name("Stone")
                        .texture("stone")
                        .buildTime(2.0f)
                        .allowedLayers("vanilla:buildings")
        );
        blocksCat.addBlock(stone);
        ctx.blocks.register("stone", stone);

        Block grass = Block.create("vanilla:grass",
                new Block.BlockProperties()
                        .name("Grass")
                        .texture("grass")
                        .solid(false)
                        .buildTime(0.5f)
                        .allowedLayers("vanilla:ground")
        );
        blocksCat.addBlock(grass);
        ctx.blocks.register("grass", grass);
    }
}