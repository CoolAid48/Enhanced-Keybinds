package me.coolaid.enhancedkeybinds.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.enhancedkeybinds.config.EnhancedKeybindsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.PlayerModelPart;

import java.util.ArrayList;
import java.util.List;

public final class IndividualSkinLayerTogglesBinds {
    private static final List<SkinLayerBinding> BINDINGS = new ArrayList<>(7);

    private IndividualSkinLayerTogglesBinds() {
    }

    public static void init() {
        EnhancedKeybindsConfig.Data config = EnhancedKeybindsConfig.data();
        register(config.registerToggleCapeKeybind, "key.enhancedkeybinds.toggle_cape", PlayerModelPart.CAPE);
        register(config.registerToggleHatKeybind, "key.enhancedkeybinds.toggle_hat", PlayerModelPart.HAT);
        register(config.registerToggleJacketKeybind, "key.enhancedkeybinds.toggle_jacket", PlayerModelPart.JACKET);
        register(config.registerToggleLeftSleeveKeybind, "key.enhancedkeybinds.toggle_left_sleeve", PlayerModelPart.LEFT_SLEEVE);
        register(config.registerToggleRightSleeveKeybind, "key.enhancedkeybinds.toggle_right_sleeve", PlayerModelPart.RIGHT_SLEEVE);
        register(config.registerToggleLeftPantsKeybind, "key.enhancedkeybinds.toggle_left_pants", PlayerModelPart.LEFT_PANTS_LEG);
        register(config.registerToggleRightPantsKeybind, "key.enhancedkeybinds.toggle_right_pants", PlayerModelPart.RIGHT_PANTS_LEG);

        ClientTickEvents.END_CLIENT_TICK.register(IndividualSkinLayerTogglesBinds::handle);
    }

    private static void handle(Minecraft client) {
        if (client.player == null) {
            return;
        }

        for (SkinLayerBinding binding : BINDINGS) {
            while (binding.keyMapping().consumeClick()) {
                toggleSkinLayer(client, binding.modelPart());
            }
        }
    }

    private static void toggleSkinLayer(Minecraft client, PlayerModelPart part) {
        client.options.setModelPart(part, !client.options.isModelPartEnabled(part));
    }

    private static void register(boolean enabled, String translationKey, PlayerModelPart modelPart) {
        if (!enabled) {
            return;
        }

        KeyMapping keyMapping = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(translationKey, InputConstants.UNKNOWN.getValue(), RegisterCategories.SKIN_CUSTOMIZATION)
        );
        BINDINGS.add(new SkinLayerBinding(keyMapping, modelPart));
    }

    private record SkinLayerBinding(KeyMapping keyMapping, PlayerModelPart modelPart) {
    }
}
