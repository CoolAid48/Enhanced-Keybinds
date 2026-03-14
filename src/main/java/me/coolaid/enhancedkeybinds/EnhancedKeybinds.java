package me.coolaid.enhancedkeybinds;

import me.coolaid.enhancedkeybinds.config.EnhancedKeybindsConfig;
import me.coolaid.enhancedkeybinds.keybindings.BasicSkinLayerTogglesBinds;
import me.coolaid.enhancedkeybinds.keybindings.ControlsTogglesBinds;
import me.coolaid.enhancedkeybinds.keybindings.IndividualSkinLayerTogglesBinds;
import net.fabricmc.api.ClientModInitializer;

public class EnhancedKeybinds implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        EnhancedKeybindsConfig.load();

        if (EnhancedKeybindsConfig.data().registerControlsKeybinds) {
            ControlsTogglesBinds.init();
        }

        if (EnhancedKeybindsConfig.data().registerSkinLayerKeybinds) {
            BasicSkinLayerTogglesBinds.init();

            if (EnhancedKeybindsConfig.data().includeIndividualSkinLayers) {
                IndividualSkinLayerTogglesBinds.init();
            }
        }
    }
}