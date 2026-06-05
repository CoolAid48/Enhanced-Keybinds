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

    public static final String MOD_ID = "Enhanced Keybinds";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        EnhancedKeybindsConfig.load();
        ControlsTogglesBinds.init();
        BasicSkinLayerTogglesBinds.init();
        IndividualSkinLayerTogglesBinds.init();
        ControllingAmecsCompat.init();

        LOGGER.info("The Keybinds are Enhancing... Check out my Hardcore World on Twitch");
        LOGGER.info("Thank you Siphalor for the Amecs key modifiers implementation!");
        LOGGER.info("And shout out to my friends for the mod assets! :D");
    }
}
