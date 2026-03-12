package com.kohs.optimizer.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.kohs.optimizer.KohsPlayerOptimizerMod;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class OptimizerConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(KohsPlayerOptimizerMod.MOD_ID + "/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
        FabricLoader.getInstance().getConfigDir().resolve("kohs-player-optimizer.json");

    private static OptimizerConfig config = new OptimizerConfig();

    private OptimizerConfigManager() {
    }

    public static synchronized OptimizerConfig getConfig() {
        return config;
    }

    public static synchronized void setConfig(OptimizerConfig config) {
        OptimizerConfigManager.config = config;
    }

    public static synchronized void load() {
        if (!Files.exists(CONFIG_PATH)) {
            config = new OptimizerConfig();
            config.normalize();
            PresetApplier.ensureCustomSnapshot(config);
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            OptimizerConfig loaded = GSON.fromJson(reader, OptimizerConfig.class);
            if (loaded != null) {
                loaded.normalize();
                PresetApplier.ensureCustomSnapshot(loaded);
                config = loaded;
            }
        } catch (IOException | JsonParseException exception) {
            LOGGER.warn("Failed to load config, restoring defaults: {}", CONFIG_PATH, exception);
            config = new OptimizerConfig();
            config.normalize();
            PresetApplier.ensureCustomSnapshot(config);
            save();
        }
    }

    public static synchronized void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(config, writer);
            }
        } catch (IOException exception) {
            LOGGER.error("Failed to save config: {}", CONFIG_PATH, exception);
        }
    }
}
