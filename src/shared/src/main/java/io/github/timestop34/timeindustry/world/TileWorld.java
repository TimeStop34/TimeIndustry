package io.github.timestop34.timeindustry.world;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import io.github.timestop34.timeindustry.components.BlockDefinitionComponent;
import io.github.timestop34.timeindustry.components.LayerComponent;
import io.github.timestop34.timeindustry.components.PositionComponent;
import io.github.timestop34.timeindustry.components.SizeComponent;
import io.github.timestop34.timeindustry.world.block.Block;
import io.github.timestop34.timeindustry.world.layers.Layer;
import io.github.timestop34.timeindustry.world.registry.LayerRegistry;

import java.util.*;

public class TileWorld {
    private final int width, height;
    private final Engine engine;
    private final Map<String, Entity[][]> layerGrids = new LinkedHashMap<>();

    public TileWorld(int width, int height, Engine engine) {
        this.width = width;
        this.height = height;
        this.engine = engine;
        // Создаём сетки для всех скомпилированных слоёв в порядке возрастания Y
        for (Layer layer : LayerRegistry.getSortedLayers()) {
            layerGrids.put(layer.id(), new Entity[width][height]);
        }
    }

    public boolean canPlace(Block def, int x, int y, String layerId) {
        Layer layer = LayerRegistry.get(layerId);
        if (layer == null) return false;
        if (!def.getProperties().isAllowedOn(layer)) return false; // нужно добавить в BlockProperties метод isAllowedOn(Layer)

        Entity[][] grid = layerGrids.get(layerId);
        for (int dx = 0; dx < def.getProperties().getWidth(); dx++) {
            for (int dy = 0; dy < def.getProperties().getHeight(); dy++) {
                int tx = x + dx, ty = y + dy;
                if (tx < 0 || tx >= width || ty < 0 || ty >= height) return false;
                if (grid[tx][ty] != null) return false;
            }
        }
        return true;
    }

    public void placeBlock(Block def, int x, int y, String layerId) {
        if (!canPlace(def, x, y, layerId)) return;

        Entity entity = engine.createEntity();
        entity.add(new PositionComponent(x, y));
        entity.add(new SizeComponent(def.getProperties().getWidth(), def.getProperties().getHeight()));
        entity.add(new BlockDefinitionComponent(def.getId()));
        entity.add(new LayerComponent(layerId));
        // Можно также добавить компонент с layerId, если нужно
        engine.addEntity(entity);

        Entity[][] grid = layerGrids.get(layerId);
        for (int dx = 0; dx < def.getProperties().getWidth(); dx++) {
            for (int dy = 0; dy < def.getProperties().getHeight(); dy++) {
                int tx = x + dx, ty = y + dy;
                grid[tx][ty] = entity;
            }
        }
    }

    public void breakBlock(int x, int y) {
        // Ищем сверху вниз (или снизу вверх — зависит от геймплея)
        List<Layer> layers = LayerRegistry.getSortedLayers();
        // Проходим от верхнего слоя к нижнему (обычно верхние слои перекрывают нижние)
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            Entity[][] grid = layerGrids.get(layer.id());
            Entity entity = grid[x][y];
            if (entity != null) {
                PositionComponent pos = entity.getComponent(PositionComponent.class);
                SizeComponent size = entity.getComponent(SizeComponent.class);
                if (pos != null && size != null) {
                    for (int dx = 0; dx < size.width; dx++) {
                        for (int dy = 0; dy < size.height; dy++) {
                            int tx = pos.tileX + dx, ty = pos.tileY + dy;
                            grid[tx][ty] = null;
                        }
                    }
                }
                engine.removeEntity(entity);
                return;
            }
        }
    }

    public Entity getEntityAt(int x, int y) {
        List<Layer> layers = LayerRegistry.getSortedLayers();
        for (int i = layers.size() - 1; i >= 0; i--) {
            Entity[][] grid = layerGrids.get(layers.get(i).id());
            if (grid[x][y] != null) return grid[x][y];
        }
        return null;
    }

    // Для отрисовки: можно получить список всех слоёв с их сетками
    public Map<String, Entity[][]> getLayerGrids() {
        return Collections.unmodifiableMap(layerGrids);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}