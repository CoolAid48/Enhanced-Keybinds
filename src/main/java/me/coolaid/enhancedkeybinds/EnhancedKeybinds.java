package me.coolaid.enhancedkeybinds;

import me.coolaid.enhancedkeybinds.config.EnhancedKeybindsConfig;
import me.coolaid.enhancedkeybinds.compat.ControllingAmecsCompat;
import me.coolaid.enhancedkeybinds.keybindings.BasicSkinLayerTogglesBinds;
import me.coolaid.enhancedkeybinds.keybindings.ControlsTogglesBinds;
import me.coolaid.enhancedkeybinds.keybindings.IndividualSkinLayerTogglesBinds;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnhancedKeybinds implements ClientModInitializer {

    public static final String MOD_ID = "enhancedkeybinds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        EnhancedKeybindsConfig.load();
        ControlsTogglesBinds.init();
        BasicSkinLayerTogglesBinds.init();
        IndividualSkinLayerTogglesBinds.init();
        ControllingAmecsCompat.init();

        LOGGER.info("Enhanced Keybinds initialized");
    }
}
