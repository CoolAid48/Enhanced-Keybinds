package me.coolaid.enhancedkeybinds.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class EnhancedKeybindsConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("enhancedkeybinds.json");

    private static Data data = new Data();

    private EnhancedKeybindsConfig() {
    }

    public static void load() {
        if (Files.notExists(CONFIG_PATH)) {
            save();
            return;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            Data loaded = GSON.fromJson(reader, Data.class);
            data = loaded != null ? loaded : new Data();
        } catch (IOException e) {
            data = new Data();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(CONFIG_PATH)) {
                GSON.toJson(data, writer);
            }
        } catch (IOException ignored) {
        }
    }

    public static Data data() {
        return data;
    }

    public static class Data {
        // Controls
        public boolean registerAutoJumpKeybind = true;
        public boolean registerSneakModeKeybind = true;
        public boolean registerSprintModeKeybind = true;
        public boolean registerClosedCaptionsKeybind = true;
        // Skin Customization
        public boolean registerSwapMainHandKeybind = true;
        public boolean registerToggleAllSkinLayersKeybind = true;
        public boolean registerToggleCapeKeybind = true;
        public boolean registerToggleHatKeybind = true;
        public boolean registerToggleJacketKeybind = true;
        public boolean registerToggleLeftSleeveKeybind = true;
        public boolean registerToggleRightSleeveKeybind = true;
        public boolean registerToggleLeftPantsKeybind = true;
        public boolean registerToggleRightPantsKeybind = true;
    }
}