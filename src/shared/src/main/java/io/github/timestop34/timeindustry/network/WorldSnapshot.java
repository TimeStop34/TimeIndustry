package io.github.timestop34.timeindustry.network;

import java.util.ArrayList;
import java.util.List;

public class WorldSnapshot {
    public List<BlockData> blocks = new ArrayList<>();
    public List<ProcessData> processes = new ArrayList<>();

    public static class BlockData {
        public int x, y;       // позиция в тайлах (левый верхний угол)
        public int width, height;
        public String layerId;
        public String blockId; // строковый ID блока (из реестра)
    }

    public static class ProcessData {
        public int x, y;
        public String blockId;
        public int width, height;   // размеры блока в тайлах
        public float progress;   // 0..1
        public boolean isBuilding;
        public String layerId;
    }
}