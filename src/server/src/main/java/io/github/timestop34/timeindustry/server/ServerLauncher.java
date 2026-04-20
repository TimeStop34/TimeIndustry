package io.github.timestop34.timeindustry.server;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import com.badlogic.gdx.backends.headless.HeadlessApplicationConfiguration;
import com.google.gson.Gson;
import io.github.timestop34.timeindustry.components.BlockDefinitionComponent;
import io.github.timestop34.timeindustry.components.LayerComponent;
import io.github.timestop34.timeindustry.mod.ModManager;
import io.github.timestop34.timeindustry.network.NetworkManager;
import io.github.timestop34.timeindustry.network.NetworkSystem;
import io.github.timestop34.timeindustry.network.ServerCommandHandler;
import io.github.timestop34.timeindustry.network.ServerEventListener;
import io.github.timestop34.timeindustry.network.messages.BaseMessage;
import io.github.timestop34.timeindustry.network.messages.StartBreakingCommand;
import io.github.timestop34.timeindustry.network.messages.StartBuildingCommand;
import io.github.timestop34.timeindustry.processes.ConstructionProcess;
import io.github.timestop34.timeindustry.world.block.Block;
import io.github.timestop34.timeindustry.world.layers.Layer;
import io.github.timestop34.timeindustry.world.registry.BlockRegistry;
import io.github.timestop34.timeindustry.world.TileWorld;
import io.github.timestop34.timeindustry.world.registry.LayerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ServerLauncher implements ApplicationListener, ServerEventListener {
    private static final Logger logger = LoggerFactory.getLogger(ServerLauncher.class);

    private Engine engine;
    private TileWorld world;
    private NetworkSystem netSystem;
    private final List<ConstructionProcess> processes = new ArrayList<>();
    private final Gson gson = new Gson();

    // ---------- Обработчик команд от клиентов ----------
    private class MyCommandHandler implements ServerCommandHandler {
        @Override
        public void handleCommand(Object playerId, BaseMessage base, String rawJson) {
            switch (base.type) {
                case "start_building":
                    StartBuildingCommand buildCmd = gson.fromJson(rawJson, StartBuildingCommand.class);
                    handleStartBuilding(playerId, buildCmd);
                    break;
                case "start_breaking":
                    StartBreakingCommand breakCmd = gson.fromJson(rawJson, StartBreakingCommand.class);
                    handleStartBreaking(playerId, breakCmd);
                    break;
                default:
                    logger.warn("Unknown command type: {}", base.type);
            }
        }
    }

    private void handleStartBuilding(Object playerId, StartBuildingCommand cmd) {
        Block def = BlockRegistry.get(cmd.blockId);
        if (def == null) {
            logger.debug("Unknown block id: {}", cmd.blockId);
            return;
        }

        // Найти самый нижний слой, на котором можно разместить блок
        List<Layer> layers = LayerRegistry.getSortedLayers(); // уже отсортированы от нижнего к верхнему
        String targetLayer = null;
        for (Layer layer : layers) {
            if (world.canPlace(def, cmd.x, cmd.y, layer.id())) {
                targetLayer = layer.id();
                break;
            }
        }

        if (targetLayer == null) {
            logger.debug("Cannot place {} at {},{} on any layer", cmd.blockId, cmd.x, cmd.y);
            return;
        }

        ConstructionProcess proc = getProcessAt(cmd.x, cmd.y);
        if (proc == null) {
            proc = new ConstructionProcess(cmd.x, cmd.y, cmd.blockId, targetLayer, def,
                    def.getProperties().getBuildTime());
            processes.add(proc);
            logger.debug("Created new build process at {},{} on layer {}", cmd.x, cmd.y, targetLayer);
        }
        proc.addBuilder(playerId);
        logger.debug("Player {} now building at {},{} (progress={})", playerId, cmd.x, cmd.y, proc.progress);
    }

    private void handleStartBreaking(Object playerId, StartBreakingCommand cmd) {
        // 1. Проверяем, есть ли активный процесс в этой клетке
        ConstructionProcess proc = getProcessAt(cmd.x, cmd.y);
        if (proc != null) {
            proc.addBreaker(playerId);
            logger.debug("Player {} joined breaking process at {},{} (progress={})", playerId, cmd.x, cmd.y, proc.progress);
            return;
        }

        // 2. Процесса нет – пытаемся найти существующий блок в мире
        Entity entity = world.getEntityAt(cmd.x, cmd.y);
        if (entity == null) {
            logger.debug("No entity or process at {},{}", cmd.x, cmd.y);
            return;
        }

        BlockDefinitionComponent defComp = entity.getComponent(BlockDefinitionComponent.class);
        LayerComponent layerComp = entity.getComponent(LayerComponent.class);
        if (defComp == null || layerComp == null) return;

        Block def = BlockRegistry.get(defComp.blockId);
        if (def == null) return;

        String layerId = layerComp.layerId;

        // 3. Удаляем блок из мира немедленно
        boolean removed = world.breakBlock(cmd.x, cmd.y);
        if (removed) {
            // 4. Создаём новый процесс разрушения
            proc = new ConstructionProcess(cmd.x, cmd.y, defComp.blockId, layerId, def,
                    def.getProperties().getBuildTime());
            processes.add(proc);
            proc.addBreaker(playerId);
            logger.debug("Started new breaking process at {},{} on layer {}", cmd.x, cmd.y, layerId);
        } else {
            logger.debug("Cannot break block at {},{} (unbreakable layer or no block)", cmd.x, cmd.y);
            return;
        }
    }

    // ---------- Удаление игрока из всех процессов ----------
    private void removePlayerFromAllProcesses(Object playerId) {
        Iterator<ConstructionProcess> it = processes.iterator();
        while (it.hasNext()) {
            ConstructionProcess p = it.next();
            p.removePlayer(playerId);
            if (p.isEmpty()) {
                logger.debug("Process at ({},{}) removed (no players left)", p.x, p.y);
                it.remove();
            }
        }
    }

    // ---------- Вызывается при отключении игрока ----------
    @Override
    public void onPlayerDisconnected(Object playerId) {
        logger.info("Player {} disconnected, removing from all processes", playerId);
        removePlayerFromAllProcesses(playerId);
    }

    // ---------- Поиск процесса по координатам ----------
    private ConstructionProcess getProcessAt(int x, int y) {
        for (ConstructionProcess p : processes) {
            if (p.x == x && p.y == y) return p;
        }
        return null;
    }

    // ---------- Обновление всех процессов (вызывается каждый кадр) ----------
    private void updateProcesses(float delta) {
        Iterator<ConstructionProcess> it = processes.iterator();
        while (it.hasNext()) {
            ConstructionProcess p = it.next();

            // Проверка валидности слоя
            Layer layer = LayerRegistry.get(p.layerId);
            if (layer == null) {
                logger.debug("Process at {},{} removed: layer {} not found", p.x, p.y, p.layerId);
                it.remove();
                continue;
            }
            boolean building = p.progress > 0;
            boolean breaking = p.progress < 0;
            if ((building && layer.unbuildable()) || (breaking && layer.unbreakable()) ||
                    (layer.unbuildable() && layer.unbreakable())) {
                logger.debug("Process at {},{} removed: layer {} no longer allows action (building={}, breaking={})",
                        p.x, p.y, p.layerId, building, breaking);
                it.remove();
                continue;
            }

            p.updateProgress(delta);

            if (p.isCompleted()) {
                if (p.isBuilt()) {
                    Block def = BlockRegistry.get(p.blockId);
                    if (def != null && world.canPlace(def, p.x, p.y, p.layerId)) {
                        world.placeBlock(def, p.x, p.y, p.layerId);
                        logger.info("Block {} built at {},{} on layer {}", p.blockId, p.x, p.y, p.layerId);
                    } else {
                        logger.debug("Building process at {},{} cancelled: canPlace failed", p.x, p.y);
                    }
                } else if (p.isBroken()) {
                    // TODO: выдать ресурсы
                    logger.info("Block broken at {},{} on layer {}", p.x, p.y, p.layerId);
                }
                it.remove();
                netSystem.sendWorldSnapshot();
            }
        }
    }

    // ---------- Инициализация сервера ----------
    @Override
    public void create() {
        logger.info("Loading mods...");
        ModManager.loadMods(new File("mods"));

        engine = new Engine();

        NetworkManager serverNetManager = new ServerNetworkManager(8080); // фиксированный порт для удобства
        netSystem = new NetworkSystem(serverNetManager, true);
        netSystem.setEngine(engine);
        netSystem.setCommandHandler(new MyCommandHandler());
        netSystem.setServerEventListener(this);
        netSystem.setProcesses(processes);
        engine.addSystem(netSystem);

        world = new TileWorld(100, 100, engine);
        // Размещаем стартовый блок для теста
        Block stone = BlockRegistry.get("vanilla:stone");
        if (stone != null) {
            world.placeBlock(stone, 10, 10, "engine:ground");
            logger.info("Placed initial stone block at (10,10)");
        } else {
            logger.error("Vanilla stone block not found! Check mods folder.");
        }

        logger.info("Server started on port 8080");
    }

    // ---------- Главный цикл ----------
    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        updateProcesses(delta);
        engine.update(delta);
    }

    @Override
    public void resize(int width, int height) {}
    @Override
    public void pause() {}
    @Override
    public void resume() {}
    @Override
    public void dispose() {
        logger.info("Server shutting down");
    }

    public static void main(String[] args) {
        HeadlessApplicationConfiguration config = new HeadlessApplicationConfiguration();
        new HeadlessApplication(new ServerLauncher(), config);
    }
}