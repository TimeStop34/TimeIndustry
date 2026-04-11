package io.github.timestop34.timeindustry.mod;

import io.github.timestop34.timeindustry.world.registry.LayerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ModManager {
    private static final Map<String, ModInitializer> loadedMods = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(ModManager.class);

    public static void loadMods(File modsFolder) {
        if (!modsFolder.exists()) {
            if (modsFolder.mkdirs()) logger.debug("Mods folder created: {}", modsFolder.getAbsolutePath());
            return;
        }

        File[] jars = modsFolder.listFiles((dir, name) -> name.endsWith(".jar"));
        if (jars == null) return;

        for (File jar : jars) {
            try (JarFile jarFile = new JarFile(jar)) {
                URLClassLoader loader = new URLClassLoader(
                    new URL[]{jar.toURI().toURL()},
                    ModManager.class.getClassLoader()
                );
                Enumeration<JarEntry> entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String entryName = entry.getName();
                    if (entryName.endsWith(".class")) {
                        String className = entryName.replace('/', '.').replace(".class", "");
                        try {
                            Class<?> clazz = loader.loadClass(className);
                            if (clazz.isAnnotationPresent(Mod.class) && ModInitializer.class.isAssignableFrom(clazz)) {
                                Mod annotation = clazz.getAnnotation(Mod.class);
                                ModInitializer initializer = (ModInitializer) clazz.getDeclaredConstructor().newInstance();
                                ModHelper hlp = new ModHelper(annotation.modId());
                                initializer.init(hlp);
                                loadedMods.put(annotation.modId(), initializer);
                                logger.debug("Loaded mod: {} ({} v{})", annotation.name(), annotation.modId(), annotation.version());
                            }
                        } catch (Exception e) {
                            System.err.println("Failed to load class " + className + " from " + jar.getName());
                            logger.error(Arrays.toString(e.getStackTrace()));
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Failed to load mod jar: " + jar.getName());
                logger.error(Arrays.toString(e.getStackTrace()));
            }
        }

        // Компиляция после
        LayerRegistry.compile();
    }

    public static Collection<ModInitializer> getMods() {
        return loadedMods.values();
    }
}
