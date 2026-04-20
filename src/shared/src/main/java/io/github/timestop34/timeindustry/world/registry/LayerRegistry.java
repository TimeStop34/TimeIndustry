package io.github.timestop34.timeindustry.world.registry;

import io.github.timestop34.timeindustry.world.layers.Layer;
import io.github.timestop34.timeindustry.world.layers.LayerProperties;

import java.util.*;

public class LayerRegistry {
    private static final Map<String, Layer> LAYERS = new LinkedHashMap<>();
    private static final List<LayerProperties> pendingProperties = new ArrayList<>();
    private static List<Layer> sortedLayers = new ArrayList<>();

    /**
     * Добавляет LayerProperties для последующей компиляции.
     * Вызывается модами через LayersSubRegistry.
     */
    public static void addProperties(LayerProperties properties) {
        pendingProperties.add(properties);
    }

    /**
     * Компилирует все добавленные LayerProperties, разрешает зависимости и создаёт Layer с y.
     * @throws IllegalStateException если обнаружен циклический граф
     */
    public static void compile() {
        if (pendingProperties.isEmpty()) return;

        // Строим граф
        Map<String, Set<String>> graph = new HashMap<>();
        Map<String, LayerProperties> propsById = new HashMap<>();

        for (LayerProperties prop : pendingProperties) {
            propsById.put(prop.getId(), prop);
            graph.putIfAbsent(prop.getId(), new HashSet<>());
        }

        for (LayerProperties prop : pendingProperties) {
            String id = prop.getId();
            for (String aboveId : prop.getAbove()) {
                if (!propsById.containsKey(aboveId)) {
                    throw new IllegalArgumentException("Unknown layer id in 'above': " + aboveId);
                }
                graph.computeIfAbsent(aboveId, k -> new HashSet<>()).add(id);
            }
            for (String belowId : prop.getBelow()) {
                if (!propsById.containsKey(belowId)) {
                    throw new IllegalArgumentException("Unknown layer id in 'below': " + belowId);
                }
                graph.computeIfAbsent(id, k -> new HashSet<>()).add(belowId);
            }
        }

        // Алгоритм Кана
        Map<String, Integer> inDegree = new HashMap<>();
        for (String node : graph.keySet()) {
            inDegree.put(node, 0);
        }
        for (Set<String> edges : graph.values()) {
            for (String neighbor : edges) {
                inDegree.merge(neighbor, 1, Integer::sum);
            }
        }

        Queue<String> queue = new LinkedList<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }

        List<String> sortedIds = new ArrayList<>();
        while (!queue.isEmpty()) {
            String node = queue.poll();
            sortedIds.add(node);
            for (String neighbor : graph.getOrDefault(node, Collections.emptySet())) {
                int newDegree = inDegree.merge(neighbor, -1, Integer::sum);
                if (newDegree == 0) {
                    queue.add(neighbor);
                }
            }
        }

        if (sortedIds.size() != graph.size()) {
            throw new IllegalStateException("Cycle detected in layer dependencies!");
        }

        // Создаём Layer с последовательными y
        LAYERS.clear();
        int y = 0;
        for (String id : sortedIds) {
            LayerProperties prop = propsById.get(id);
            Layer layer = new Layer(prop.getId(), prop.getName(), prop.getIconPath(), y++, prop.isUnbreakable(), prop.isUnbuildable());
            LAYERS.put(id, layer);
        }

        sortedLayers = new ArrayList<>(LAYERS.values());
        sortedLayers.sort(Comparator.comparingInt(Layer::y));

        pendingProperties.clear();
    }

    public static Layer get(String id) {
        return LAYERS.get(id);
    }

    public static List<Layer> getSortedLayers() {
        return Collections.unmodifiableList(sortedLayers);
    }

    public static void reset() {
        LAYERS.clear();
        sortedLayers.clear();
        pendingProperties.clear();
    }
}