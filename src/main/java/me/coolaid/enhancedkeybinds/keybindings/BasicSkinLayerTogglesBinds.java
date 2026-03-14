package me.coolaid.enhancedkeybinds.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelPart;

public final class BasicSkinLayerTogglesBinds {
    private static KeyMapping toggleAllSkinLayers;
    private static KeyMapping toggleOffhand;

    private BasicSkinLayerTogglesBinds() {
    }

    public static void init() {
        toggleAllSkinLayers = register("key.enhancedkeybinds.toggle_all");
        toggleOffhand = register("key.enhancedkeybinds.swap_offhand");
        ClientTickEvents.END_CLIENT_TICK.register(BasicSkinLayerTogglesBinds::handle);
    }

    private static void handle(Minecraft client) {
        if (client.player == null) {
            return;
        }

        while (toggleAllSkinLayers.consumeClick()) {
            toggleAllSkinLayers(client);
        }

        while (toggleOffhand.consumeClick()) {
            HumanoidArm newArm = client.options.mainHand().get() == HumanoidArm.RIGHT ? HumanoidArm.LEFT : HumanoidArm.RIGHT;
            client.options.mainHand().set(newArm);
            client.player.setMainArm(newArm);
            client.options.save();
            client.player.displayClientMessage(Component.translatable(newArm == HumanoidArm.RIGHT ? "message.main-hand.enabled" : "message.main-hand.disabled"), true);
        }
    }

    private static void toggleAllSkinLayers(Minecraft client) {
        boolean anyDisabled = false;
        for (PlayerModelPart part : PlayerModelPart.values()) {
            if (!client.options.isModelPartEnabled(part)) {
                anyDisabled = true;
                break;
            }
        }

        for (PlayerModelPart part : PlayerModelPart.values()) {
            client.options.setModelPart(part, anyDisabled);
        }

        client.player.displayClientMessage(Component.translatable(anyDisabled ? "message.skin-layers.enabled" : "message.skin-layers.disabled"), true);
    }

    private static KeyMapping register(String translationKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(translationKey, InputConstants.UNKNOWN.getValue(), RegisterCategories.SKIN_LAYERS));
    }
}