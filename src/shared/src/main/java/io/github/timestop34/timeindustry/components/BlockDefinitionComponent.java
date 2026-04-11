package io.github.timestop34.timeindustry.components;

import com.badlogic.ashley.core.Component;

public class BlockDefinitionComponent implements Component {
    public String blockId;
    public BlockDefinitionComponent(String id) { blockId = id; }
}

