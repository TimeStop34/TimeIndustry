package io.github.timestop34.timeindustry.systems;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import io.github.timestop34.timeindustry.components.BlockDefinitionComponent;
import io.github.timestop34.timeindustry.components.LayerComponent;
import io.github.timestop34.timeindustry.components.PositionComponent;
import io.github.timestop34.timeindustry.components.SizeComponent;
import io.github.timestop34.timeindustry.network.WorldSnapshot;
import io.github.timestop34.timeindustry.world.block.Block;
import io.github.timestop34.timeindustry.world.registry.BlockRegistry;
import io.github.timestop34.timeindustry.utils.TextureCache;
import io.github.timestop34.timeindustry.world.layers.Layer;
import io.github.timestop34.timeindustry.world.registry.LayerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RenderSystem extends EntitySystem {
    public static final int TILE_SIZE = 32;
    public static final float CAMERA_SPEED = 300f;

    public SpriteBatch batch;
    public OrthographicCamera camera;
    public Viewport viewport;
    private ShapeRenderer shapeRenderer;

    private static final Logger logger = LoggerFactory.getLogger(RenderSystem.class);

    private List<WorldSnapshot.ProcessData> processes = new ArrayList<>();

    public RenderSystem(SpriteBatch batch) {
        this.batch = batch;
        camera = new OrthographicCamera();
        viewport = new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), camera);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
        shapeRenderer = new ShapeRenderer();
    }

    public void setProcesses(List<WorldSnapshot.ProcessData> processes) {
        this.processes = processes != null ? processes : new ArrayList<>();
    }

    public void resize(int width, int height) {
        viewport.update(width, height, true);
        camera.position.set(viewport.getWorldWidth() / 2, viewport.getWorldHeight() / 2, 0);
        camera.update();
    }

    @Override
    public void update(float deltaTime) {
        // 1. Получаем все блоки с компонентами (теперь обязательно LayerComponent)
        ImmutableArray<Entity> entities = getEngine().getEntitiesFor(
                Family.all(PositionComponent.class, SizeComponent.class,
                        BlockDefinitionComponent.class, LayerComponent.class).get()
        );

        // 2. Группируем сущности по layerId
        Map<String, List<Entity>> entitiesByLayer = new HashMap<>();
        for (Entity entity : entities) {
            LayerComponent layerComp = entity.getComponent(LayerComponent.class);
            if (layerComp == null) continue;
            entitiesByLayer.computeIfAbsent(layerComp.layerId, k -> new ArrayList<>()).add(entity);
        }

        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(Color.WHITE);

        // 3. Получаем отсортированные слои из реестра (от нижнего к верхнему)
        List<Layer> sortedLayers = LayerRegistry.getSortedLayers();

        // 4. Рисуем слои в порядке возрастания Y (ground -> buildings -> upper)
        for (Layer layer : sortedLayers) {
            List<Entity> layerEntities = entitiesByLayer.get(layer.id());
            if (layerEntities == null) continue;

            for (Entity entity : layerEntities) {
                PositionComponent pos = entity.getComponent(PositionComponent.class);
                SizeComponent size = entity.getComponent(SizeComponent.class);
                BlockDefinitionComponent def = entity.getComponent(BlockDefinitionComponent.class);

                Block block = BlockRegistry.get(def.blockId);
                if (block == null) continue;

                Texture texture = TextureCache.getTexture(block.getTexturePath());
                float x = pos.tileX * TILE_SIZE;
                float y = pos.tileY * TILE_SIZE;
                float w = size.width * TILE_SIZE;
                float h = size.height * TILE_SIZE;

                // Ищем процесс для этого блока (по координатам и слою)
                WorldSnapshot.ProcessData process = findProcessAt(pos.tileX, pos.tileY, layer.id());

                if (process != null) {
                    float health = (process.progress + 1f) / 2f;
                    batch.setColor(1f, 1f, 1f, health);
                } else {
                    batch.setColor(Color.WHITE);
                }

                batch.draw(texture, x, y, w, h);
            }
        }
        batch.end();

        // 5. Рисуем рамки и прозрачности для процессов (поверх всех слоёв)
        if (!processes.isEmpty()) {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            for (WorldSnapshot.ProcessData p : processes) {
                float x = p.x * TILE_SIZE;
                float y = p.y * TILE_SIZE;
                float w = p.width * TILE_SIZE;
                float h = p.height * TILE_SIZE;

                shapeRenderer.setColor(p.progress > 0 ? Color.YELLOW : Color.RED);
                shapeRenderer.rect(x, y, w, h);
            }

            shapeRenderer.end();
            Gdx.gl.glDisable(GL20.GL_BLEND);
        }
    }

    // Обновлённый метод поиска процесса (теперь учитывает слой)
    private WorldSnapshot.ProcessData findProcessAt(int x, int y, String layerId) {
        for (WorldSnapshot.ProcessData p : processes) {
            if (p.x == x && p.y == y && p.layerId.equals(layerId)) {
                return p;
            }
        }
        return null;
    }

    public void dispose() {
        TextureCache.dispose();
        shapeRenderer.dispose();
    }
}