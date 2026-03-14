package me.coolaid.enhancedkeybinds.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
        toggleCape = register("key.enhancedkeybinds.toggle_cape");
        toggleHat = register("key.enhancedkeybinds.toggle_hat");
        toggleJacket = register("key.enhancedkeybinds.toggle_jacket");
        toggleLeftSleeve = register("key.enhancedkeybinds.toggle_left_sleeve");
        toggleRightSleeve = register("key.enhancedkeybinds.toggle_right_sleeve");
        toggleLeftPants = register("key.enhancedkeybinds.toggle_left_pants");
        toggleRightPants = register("key.enhancedkeybinds.toggle_right_pants");

        ClientTickEvents.END_CLIENT_TICK.register(IndividualSkinLayerTogglesBinds::handle);
    }

    private static void handle(Minecraft client) {
        if (client.player == null) {
            return;
        }

        while (toggleCape.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.CAPE, "key.enhancedkeybinds.toggle_cape");
        }

        while (toggleHat.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.HAT, "key.enhancedkeybinds.toggle_hat");
        }

        while (toggleJacket.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.JACKET, "key.enhancedkeybinds.toggle_jacket");
        }

        while (toggleLeftSleeve.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.LEFT_SLEEVE, "key.enhancedkeybinds.toggle_left_sleeve");
        }

        while (toggleRightSleeve.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.RIGHT_SLEEVE, "key.enhancedkeybinds.toggle_right_sleeve");
        }

        while (toggleLeftPants.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.LEFT_PANTS_LEG, "key.enhancedkeybinds.toggle_left_pants");
        }

        while (toggleRightPants.consumeClick()) {
            toggleSkinLayer(client, PlayerModelPart.RIGHT_PANTS_LEG, "key.enhancedkeybinds.toggle_right_pants");
        }
    }

    private static void toggleSkinLayer(Minecraft client, PlayerModelPart part, String labelTranslationKey) {
        boolean enabled = !client.options.isModelPartEnabled(part);
        client.options.setModelPart(part, enabled);

        client.player.displayClientMessage(
                Component.translatable(
                        "enhancedkeybinds.message.toggled",
                        Component.translatable(labelTranslationKey),
                        Component.translatable(enabled ? "enhancedkeybinds.state.on" : "enhancedkeybinds.state.off")
                ),
                true
        );
    }

    private static KeyMapping register(String translationKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(translationKey, InputConstants.UNKNOWN.getValue(), RegisterCategories.SKIN_LAYERS));
    }
}