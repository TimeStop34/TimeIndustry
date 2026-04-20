package io.github.timestop34.timeindustry.network;

import java.util.ArrayList;
import java.util.List;

public class WorldSnapshot {
    public List<BlockData> blocks = new ArrayList<>();

    public static class BlockData {
        public int x, y;
        public int width, height;
        public String layerId;
        public String blockId;
    }
}