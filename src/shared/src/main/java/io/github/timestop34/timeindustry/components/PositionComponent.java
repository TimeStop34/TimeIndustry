package io.github.timestop34.timeindustry.components;

import com.badlogic.ashley.core.Component;

public class PositionComponent implements Component {
    public int tileX, tileY; // левый верхний угол в тайлах
    public PositionComponent(int x, int y) { tileX = x; tileY = y; }
}
