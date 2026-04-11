package io.github.timestop34.timeindustry.world.block;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.physics.box2d.World;
import io.github.timestop34.timeindustry.world.layers.Layer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class Block {
    protected final String id;
    protected final BlockProperties properties;

    // Приватный конструктор для внутреннего использования
    protected Block(String id, BlockProperties properties) {
        this.id = id;
        // Клонируем, чтобы внешний код не мог изменить свойства после регистрации
        this.properties = properties.clone();
    }

    // Фабричный метод для создания простых блоков (пол, стены и т.д.)
    public static Block create(String id, BlockProperties properties) {
        return new Block(id, properties) {};
    }

    public String getId() {
        return id;
    }

    public BlockProperties getProperties() {
        return properties.clone();
    }

    // Вспомогательный метод для получения пути к текстуре
    public String getTexturePath() {
        String[] parts = id.split(":");
        String modId = parts[0];
        return "rsrc/" + modId + "/textures/blocks/" + properties.textureName + ".png";
    }

    // Методы, которые могут быть переопределены функциональными блоками
    public void onPlace(World world, int x, int y, Entity entity) {}
    public void onBreak(World world, int x, int y, Entity entity) {}
    public void update(World world, int x, int y, Entity entity, float delta) {}

    // Вложенный класс свойств с паттерном "строитель"
    public static class BlockProperties implements Cloneable {
        private String name = "";
        private int width = 1;
        private int height = 1;
        private String textureName = "missing";
        private boolean solid = true;
        private float buildTime = 1.0f;
        private Set<String> allowedLayers = new HashSet<>(); // пустой = нигде нельзя

        // Методы установки (возвращают this для цепочек)
        public BlockProperties name(String name) {
            this.name = name;
            return this;
        }

        public BlockProperties size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public BlockProperties texture(String textureName) {
            this.textureName = textureName;
            return this;
        }

        public BlockProperties solid(boolean solid) {
            this.solid = solid;
            return this;
        }

        public BlockProperties buildTime(float seconds) {
            this.buildTime = seconds;
            return this;
        }

        public BlockProperties allowedLayers(String... layerIds) {
            this.allowedLayers = new HashSet<>(Arrays.asList(layerIds));
            return this;
        }

        // Геттеры для доступа к значениям
        public String getName() {
            return name;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public String getTextureName() {
            return textureName;
        }

        public boolean isSolid() {
            return solid;
        }

        public float getBuildTime() {
            return buildTime;
        }

        public boolean isAllowedOn(Layer layer) {
            return allowedLayers.contains(layer.id());
        }

        @Override
        public BlockProperties clone() {
            try {
                BlockProperties clone = (BlockProperties) super.clone();
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError("Clone not supported", e);
            }
        }
    }
}