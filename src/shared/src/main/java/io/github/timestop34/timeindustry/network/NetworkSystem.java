package io.github.timestop34.timeindustry.network;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.google.gson.Gson;
import io.github.timestop34.timeindustry.components.BlockDefinitionComponent;
import io.github.timestop34.timeindustry.components.LayerComponent;
import io.github.timestop34.timeindustry.components.PositionComponent;
import io.github.timestop34.timeindustry.components.SizeComponent;
import io.github.timestop34.timeindustry.network.messages.BaseMessage;
import io.github.timestop34.timeindustry.network.messages.WorldSnapshotMessage;
import io.github.timestop34.timeindustry.processes.ConstructionProcess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class NetworkSystem extends EntitySystem implements NetworkListener {
    private final NetworkManager networkManager;
    private Engine engine;
    private final boolean isServer;
    private float accumulator = 0f;
    private static final float SNAPSHOT_DELAY = 0.1f;
    private final Gson gson = new Gson();
    private List<ConstructionProcess> processes; // только для сервера
    private List<WorldSnapshot.ProcessData> lastProcesses = new ArrayList<>();

    public List<WorldSnapshot.ProcessData> getLastProcesses() {
        return lastProcesses;
    }

    // Для сервера – обработчик команд
    private ServerCommandHandler commandHandler;
    private ServerEventListener serverEventListener;

    private static final Logger logger = LoggerFactory.getLogger(NetworkSystem.class);

    public NetworkSystem(NetworkManager networkManager, boolean isServer) {
        this.networkManager = networkManager;
        this.isServer = isServer;
        networkManager.setListener(this);
    }

    public void setEngine(Engine engine) {
        this.engine = engine;
    }

    public void setCommandHandler(ServerCommandHandler handler) {
        this.commandHandler = handler;
    }

    public void setServerEventListener(ServerEventListener listener) {
        this.serverEventListener = listener;
    }

    @Override
    public void update(float deltaTime) {
        if (isServer && engine != null) {
            accumulator += deltaTime;
            if (accumulator >= SNAPSHOT_DELAY) {
                accumulator = 0;
                sendWorldSnapshot();
            }
        }
    }

    public void sendWorldSnapshot() {
        if (!isServer) return;
        WorldSnapshot snapshot = new WorldSnapshot();
        ImmutableArray<Entity> entities = engine.getEntitiesFor(
                Family.all(PositionComponent.class, SizeComponent.class, BlockDefinitionComponent.class).get());
        for (Entity entity : entities) {
            PositionComponent pos = entity.getComponent(PositionComponent.class);
            SizeComponent size = entity.getComponent(SizeComponent.class);
            BlockDefinitionComponent def = entity.getComponent(BlockDefinitionComponent.class);
            WorldSnapshot.BlockData data = new WorldSnapshot.BlockData();
            data.x = pos.tileX;
            data.y = pos.tileY;
            data.width = size.width;
            data.height = size.height;
            data.blockId = def.blockId;
            LayerComponent layerComp = entity.getComponent(LayerComponent.class);
            data.layerId = layerComp.layerId;
            snapshot.blocks.add(data);
        }
        if (processes != null) {
            for (ConstructionProcess p : processes) {
                WorldSnapshot.ProcessData pd = new WorldSnapshot.ProcessData();
                pd.x = p.x;
                pd.y = p.y;
                pd.blockId = p.blockId;
                pd.width = p.block.getProperties().getWidth();
                pd.height = p.block.getProperties().getHeight();
                pd.layerId = p.layerId;
                pd.progress = p.progress;
                pd.isBuilding = p.progress > 0;
                snapshot.processes.add(pd);
            }
        }
        WorldSnapshotMessage msg = new WorldSnapshotMessage();
        msg.snapshot = snapshot;
        String json = gson.toJson(msg);
        networkManager.sendMessage(json);
    }

    private void processIncomingMessage(String id, String message) {
        try {
            BaseMessage base = gson.fromJson(message, BaseMessage.class);
            if (base != null && base.type != null) {
                if (isServer && commandHandler != null) {
                    commandHandler.handleCommand(id, base, message);
                } else if (!isServer) {
                    if ("snapshot".equals(base.type)) {
                        WorldSnapshotMessage msg = gson.fromJson(message, WorldSnapshotMessage.class);
                        if (msg != null && msg.snapshot != null && msg.snapshot.blocks != null) {
                            updateLocalWorld(msg.snapshot);
                        } else {
                            logger.warn("Received empty or invalid snapshot");
                        }
                    }
                }
                return;
            }
        } catch (Exception e) {
            logger.warn("Received not json message from server - ignoring (Client id - '{}')", id);
        }
    }

    private void updateLocalWorld(WorldSnapshot snapshot) {
        ImmutableArray<Entity> entities = engine.getEntitiesFor(
                Family.all(PositionComponent.class, SizeComponent.class, BlockDefinitionComponent.class).get());
        for (Entity entity : entities) {
            engine.removeEntity(entity);
        }
        for (WorldSnapshot.BlockData data : snapshot.blocks) {
            Entity entity = engine.createEntity();
            entity.add(new PositionComponent(data.x, data.y));
            entity.add(new SizeComponent(data.width, data.height));
            entity.add(new BlockDefinitionComponent(data.blockId));
            entity.add(new LayerComponent(data.layerId));
            engine.addEntity(entity);
        }
        // Сохраняем процессы для отрисовки
        if (snapshot.processes != null) {
            lastProcesses = snapshot.processes;
        } else {
            lastProcesses.clear();
        }
    }

    public void setProcesses(List<ConstructionProcess> processes) {
        this.processes = processes;
    }

    public void sendCommand(BaseMessage command) {
        String json = gson.toJson(command);
        networkManager.sendMessage(json);
    }

    public void dispose() throws InterruptedException {
        networkManager.close();
    }

    @Override
    public void onMessageReceived(String id, String message) {
        if (!isServer) {
            Gdx.app.postRunnable(() -> processIncomingMessage(id, message));
        } else {
            processIncomingMessage(id, message);
        }
    }

    @Override
    public void onPlayerDisconnected(String playerId) {
        if (isServer && serverEventListener != null) {
            serverEventListener.onPlayerDisconnected(playerId);
        }
    }
}