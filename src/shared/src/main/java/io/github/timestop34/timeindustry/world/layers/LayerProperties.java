package io.github.timestop34.timeindustry.world.layers;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class LayerProperties {
    private final String id;
    private final String name;
    private final String iconPath;
    private final Set<String> above = new HashSet<>();
    private final Set<String> below = new HashSet<>();
    private final boolean unbreakable;
    private final boolean unbuildable;

    public LayerProperties(String id, String name, String iconPath, boolean unbreakable, boolean unbuildable) {
        this.id = id;
        this.name = name;
        this.iconPath = iconPath;
        this.unbreakable = unbreakable;
        this.unbuildable = unbuildable;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getIconPath() { return iconPath; }
    public boolean isUnbreakable() { return unbreakable; }
    public boolean isUnbuildable() { return unbuildable; }

    public LayerProperties above(String targetId) {
        above.add(targetId);
        return this;
    }

    public LayerProperties below(String targetId) {
        below.add(targetId);
        return this;
    }

    public LayerProperties between(String lowerId, String upperId) {
        above.add(lowerId);
        below.add(upperId);
        return this;
    }

    public Set<String> getAbove() { return Collections.unmodifiableSet(above); }
    public Set<String> getBelow() { return Collections.unmodifiableSet(below); }
}