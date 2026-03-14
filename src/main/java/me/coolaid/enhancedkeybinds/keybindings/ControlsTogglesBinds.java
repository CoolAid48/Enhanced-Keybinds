package me.coolaid.enhancedkeybinds.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;

public final class ControlsTogglesBinds {
    private static KeyMapping toggleAutoJump;
    private static KeyMapping toggleSneak;
    private static KeyMapping toggleSprint;
    private static KeyMapping toggleSubtitles;

    private ControlsTogglesBinds() {
    }

    public static void init() {
        toggleAutoJump = register("key.enhancedkeybinds.toggle_auto_jump");
        toggleSneak = register("key.enhancedkeybinds.toggle_sneak");
        toggleSprint = register("key.enhancedkeybinds.toggle_sprint");
        toggleSubtitles = register("key.enhancedkeybinds.toggle_subtitles");

        ClientTickEvents.END_CLIENT_TICK.register(ControlsTogglesBinds::handle);
    }

    private static void handle(Minecraft client) {
        if (client.player == null) {
            return;
        }

        while (toggleAutoJump.consumeClick()) {
            boolean enabled = !client.options.autoJump().get();
            client.options.autoJump().set(enabled);
            client.player.displayClientMessage(Component.translatable(enabled ? "message.auto-jump.enabled" : "message.auto-jump.disabled"), true);
        }

        while (toggleSneak.consumeClick()) {
            boolean enabled = !client.options.toggleCrouch().get();
            client.options.toggleCrouch().set(enabled);
            client.player.displayClientMessage(Component.translatable(enabled ? "message.crouching.enabled" : "message.crouching.disabled"), true);
        }

        while (toggleSprint.consumeClick()) {
            boolean enabled = !client.options.toggleSprint().get();
            client.options.toggleSprint().set(enabled);
            client.player.displayClientMessage(Component.translatable(enabled ? "message.sprinting.enabled" : "message.sprinting.disabled"), true);
        }

        while (toggleSubtitles.consumeClick()) {
            boolean enabled = !client.options.showSubtitles().get();
            client.options.showSubtitles().set(enabled);
            client.player.displayClientMessage(Component.translatable(enabled ? "message.subtitles.enabled" : "message.subtitles.disabled"), true);
        }
    }

    private static KeyMapping register(String translationKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(translationKey, InputConstants.UNKNOWN.getValue(), RegisterCategories.CONTROLS));
    }
}