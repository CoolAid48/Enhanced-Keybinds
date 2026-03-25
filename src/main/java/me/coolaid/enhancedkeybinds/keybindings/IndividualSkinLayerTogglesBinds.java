package me.coolaid.enhancedkeybinds.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.enhancedkeybinds.config.EnhancedKeybindsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerModelPart;

public final class IndividualSkinLayerTogglesBinds {
    private static KeyMapping toggleCape;
    private static KeyMapping toggleHat;
    private static KeyMapping toggleJacket;
    private static KeyMapping toggleLeftSleeve;
    private static KeyMapping toggleRightSleeve;
    private static KeyMapping toggleLeftPants;
    private static KeyMapping toggleRightPants;

    private IndividualSkinLayerTogglesBinds() {
    }

    public static void init() {
        if (EnhancedKeybindsConfig.data().registerToggleCapeKeybind) {
            toggleCape = register("key.enhancedkeybinds.toggle_cape");
        }
        if (EnhancedKeybindsConfig.data().registerToggleHatKeybind) {
            toggleHat = register("key.enhancedkeybinds.toggle_hat");
        }
        if (EnhancedKeybindsConfig.data().registerToggleJacketKeybind) {
            toggleJacket = register("key.enhancedkeybinds.toggle_jacket");
        }
        if (EnhancedKeybindsConfig.data().registerToggleLeftSleeveKeybind) {
            toggleLeftSleeve = register("key.enhancedkeybinds.toggle_left_sleeve");
        }
        if (EnhancedKeybindsConfig.data().registerToggleRightSleeveKeybind) {
            toggleRightSleeve = register("key.enhancedkeybinds.toggle_right_sleeve");
        }
        if (EnhancedKeybindsConfig.data().registerToggleLeftPantsKeybind) {
            toggleLeftPants = register("key.enhancedkeybinds.toggle_left_pants");
        }
        if (EnhancedKeybindsConfig.data().registerToggleRightPantsKeybind) {
            toggleRightPants = register("key.enhancedkeybinds.toggle_right_pants");
        }

        ClientTickEvents.END_CLIENT_TICK.register(IndividualSkinLayerTogglesBinds::handle);
    }

    private static void handle(Minecraft client) {
        if (client.player == null) {
            return;
        }

        while (toggleCape != null && toggleCape.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.CAPE, "key.enhancedkeybinds.toggle_cape");
        }

        while (toggleHat != null && toggleHat.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.HAT, "key.enhancedkeybinds.toggle_hat");
        }

        while (toggleJacket != null && toggleJacket.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.JACKET, "key.enhancedkeybinds.toggle_jacket");
        }

        while (toggleLeftSleeve != null && toggleLeftSleeve.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.LEFT_SLEEVE, "key.enhancedkeybinds.toggle_left_sleeve");
        }

        while (toggleRightSleeve != null && toggleRightSleeve.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.RIGHT_SLEEVE, "key.enhancedkeybinds.toggle_right_sleeve");
        }

        while (toggleLeftPants != null && toggleLeftPants.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.LEFT_PANTS_LEG, "key.enhancedkeybinds.toggle_left_pants");
        }

        while (toggleRightPants != null && toggleRightPants.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.RIGHT_PANTS_LEG, "key.enhancedkeybinds.toggle_right_pants");
        }
    }

    private static void toggleSkinLayer(Minecraft client, PlayerModelPart part, String labelTranslationKey) {
        boolean enabled = !client.options.isModelPartEnabled(part);
        client.options.setModelPart(part, enabled);
    }

    private static KeyMapping register(String translationKey) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(translationKey, InputConstants.UNKNOWN.getValue(), RegisterCategories.SKIN_CUSTOMIZATION));
    }
}