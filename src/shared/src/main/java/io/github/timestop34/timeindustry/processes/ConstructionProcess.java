package io.github.timestop34.timeindustry.processes;

import io.github.timestop34.timeindustry.world.block.Block;

import java.util.HashSet;
import java.util.Set;

public class ConstructionProcess {
    public final int x, y;
    public final String blockId;
    public final Block block;
    public final float halfTime;   // время строительства (для прогресса от 0 до 1)
    public float progress;          // от -1 до 1
    public final String layerId;

    private final Set<Object> builders = new HashSet<>();
    private final Set<Object> breakers = new HashSet<>();

    public ConstructionProcess(int x, int y, String blockId, String layerId, Block block,
                               float fullTime) {
        this.x = x;
        this.y = y;
        this.blockId = blockId;
        this.block = block;
        this.layerId = layerId;
        this.halfTime = fullTime / 2f;
        this.progress = 0f;
    }

    public int getWidth() {
        return block.getProperties().getWidth();
    }

    public int getHeight() {
        return block.getProperties().getHeight();
    }

    public void addBuilder(Object playerId) {
        builders.add(playerId);
        breakers.remove(playerId);
    }

    public void addBreaker(Object playerId) {
        breakers.add(playerId);
        builders.remove(playerId);
    }

    public void removePlayer(Object playerId) {
        builders.remove(playerId);
        breakers.remove(playerId);
    }

    public boolean isEmpty() {
        return builders.isEmpty() && breakers.isEmpty();
    }

    public int getTotalBuilders() { return builders.size(); }
    public int getTotalBreakers() { return breakers.size(); }

    public void updateProgress(float deltaTime) {
        int b = builders.size();
        int br = breakers.size();
        if (b == 0 && br == 0) return;

        int net = b - br;
        if (net == 0) return;

        float deltaProgress = (net * deltaTime) / halfTime;
        progress += deltaProgress;

        if (progress > 1f) progress = 1f;
        if (progress < -1f) progress = -1f;
    }

    public boolean isCompleted() {
        return progress >= 1f || progress <= -1f;
    }

    public boolean isBuilt() {
        return progress >= 1f;
    }

    public boolean isBroken() {
        return progress <= -1f;
    }
}