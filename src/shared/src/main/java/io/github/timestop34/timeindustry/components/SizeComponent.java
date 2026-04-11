package io.github.timestop34.timeindustry.components;

import com.badlogic.ashley.core.Component;

public class SizeComponent implements Component {
    public int width, height;
    public SizeComponent(int w, int h) { width = w; height = h; }
}
