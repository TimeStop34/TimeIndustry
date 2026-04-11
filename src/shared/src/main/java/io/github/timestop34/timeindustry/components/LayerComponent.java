package io.github.timestop34.timeindustry.components;

import com.badlogic.ashley.core.Component;

public class LayerComponent implements Component {
    public String layerId;
    public LayerComponent(String layerId) { this.layerId = layerId; }
}
