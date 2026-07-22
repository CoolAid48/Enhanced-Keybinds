package me.coolaid.enhancedkeybinds.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.enhancedkeybinds.config.EnhancedKeybindsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

public final class BasicSkinLayerTogglesBinds {
    private static final PlayerModelPart[] MODEL_PARTS = PlayerModelPart.values();

    private static KeyMapping toggleAllSkinLayers;
    private static KeyMapping toggleOffhand;

    private BasicSkinLayerTogglesBinds() {
    }

    public static void init() {
        if (EnhancedKeybindsConfig.data().registerToggleAllSkinLayersKeybind) {
            toggleAllSkinLayers = register("key.enhancedkeybinds.toggle_all");
        }
        if (EnhancedKeybindsConfig.data().registerSwapMainHandKeybind) {
            toggleOffhand = register("key.enhancedkeybinds.swap_offhand");
        }
        ClientTickEvents.END_CLIENT_TICK.register(BasicSkinLayerTogglesBinds::handle);
    }

    private static void handle(Minecraft client) {
        if (client.player == null) {
            return;
        }

        while (toggleAllSkinLayers != null && toggleAllSkinLayers.consumeClick()) {
            toggleAllSkinLayers(client);
        }

        while (toggleOffhand != null && toggleOffhand.consumeClick()) {
            HumanoidArm newArm = client.options.mainHand().get() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            client.options.mainHand().set(newArm);
            client.player.setMainArm(newArm);
            client.options.save();
            client.player.sendOverlayMessage(
                    Component.translatable(
                            "enhancedkeybinds.actionbar.main_hand",
                            Component.translatable(newArm == HumanoidArm.RIGHT ? "enhancedkeybinds.actionbar.component.right" : "enhancedkeybinds.actionbar.component.left")
                                    .withStyle(ChatFormatting.GREEN)
                    )
            );
        }
    }

    private static void toggleAllSkinLayers(Minecraft client) {
        boolean anyDisabled = false;
        for (PlayerModelPart part : MODEL_PARTS) {
            if (!client.options.isModelPartEnabled(part)) {
                anyDisabled = true;
                break;
            }
        }

        for (PlayerModelPart part : MODEL_PARTS) {
            client.options.setModelPart(part, anyDisabled);
        }

        client.player.sendOverlayMessage(
                Component.translatable(
                        "enhancedkeybinds.actionbar.component.toggled",
                        Component.translatable("enhancedkeybinds.actionbar.skin-layers"),
                        Component.translatable(anyDisabled ? "enhancedkeybinds.actionbar.component.enabled" : "enhancedkeybinds.actionbar.component.disabled")
                                .withStyle(anyDisabled ? ChatFormatting.GREEN : ChatFormatting.RED)
                )
        );
    }

    private static KeyMapping register(String translationKey) {
        return KeyMappingHelper.registerKeyMapping(new KeyMapping(translationKey, InputConstants.UNKNOWN.getValue(), RegisterCategories.SKIN_CUSTOMIZATION));
    }
}
