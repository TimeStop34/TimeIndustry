package io.github.timestop34.timeindustry.world.block;

import java.util.ArrayList;
import java.util.List;

public class Category {
    private final String id;
    private final String name;
    private final String iconPath;
    private final List<Block> blocks = new ArrayList<>();

    public Category(String id, String name, String iconPath) {
        this.id = id;
        this.name = name;
        this.iconPath = iconPath;
    }

    public String id() { return id; }
    public String name() { return name; }
    public String iconPath() { return iconPath; }

    public void addBlock(Block block) {
        blocks.add(block);
    }

    public List<Block> getBlocks() {
        return new ArrayList<>(blocks);
    }
}