package io.github.timestop34.timeindustry.network;

import java.util.ArrayList;
import java.util.List;

public class ProcessSnapshot {
    public static class ProcessData {
        public int x, y;
        public String blockId;
        public int width, height;
        public float progress;
        public boolean isBuilding;
        public String layerId;
    }

    public List<ProcessData> processes = new ArrayList<>();
}