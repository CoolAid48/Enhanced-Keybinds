package me.coolaid.enhancedkeybinds;

import me.coolaid.enhancedkeybinds.config.EnhancedKeybindsConfig;
import net.fabricmc.api.ClientModInitializer;

public class EnhancedKeybinds implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EnhancedKeybindsConfig.load();
    }
}
