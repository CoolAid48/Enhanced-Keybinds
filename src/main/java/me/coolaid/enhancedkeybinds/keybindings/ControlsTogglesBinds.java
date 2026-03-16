package me.coolaid.enhancedkeybinds.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import me.coolaid.enhancedkeybinds.config.EnhancedKeybindsConfig;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class ControlsTogglesBinds {
    private static KeyMapping toggleAutoJump;
    private static KeyMapping toggleSneak;
    private static KeyMapping toggleSprint;
    private static KeyMapping toggleClosedCaptions;

    private ControlsTogglesBinds() {
    }

    public static void init() {
        if (EnhancedKeybindsConfig.data().registerAutoJumpKeybind) {
            toggleAutoJump = register("key.enhancedkeybinds.toggle_auto_jump");
        }
        if (EnhancedKeybindsConfig.data().registerSneakModeKeybind) {
            toggleSneak = register("key.enhancedkeybinds.toggle_sneak");
        }
        if (EnhancedKeybindsConfig.data().registerSprintModeKeybind) {
            toggleSprint = register("key.enhancedkeybinds.toggle_sprint");
        }
        if (EnhancedKeybindsConfig.data().registerClosedCaptionsKeybind) {
            toggleClosedCaptions = register("key.enhancedkeybinds.toggle_closed_captions");
        }

        ClientTickEvents.END_CLIENT_TICK.register(ControlsTogglesBinds::handle);
    }

    private static void handle(Minecraft client) {
        if (client.player == null) {
            return;
        }

        while (toggleAutoJump != null && toggleAutoJump.consumeClick()) {
            boolean enabled = !client.options.autoJump().get();
            client.options.autoJump().set(enabled);
            client.player.displayClientMessage(stateMessage("enhancedkeybinds.actionbar.auto-jump", enabled), true);
        }

        while (toggleSneak != null && toggleSneak.consumeClick()) {
            boolean enabled = !client.options.toggleCrouch().get();
            client.options.toggleCrouch().set(enabled);
            MutableComponent mode = Component.translatable(enabled ? "enhancedkeybinds.actionbar.component.toggle" : "enhancedkeybinds.actionbar.component.hold")
                    .withStyle(ChatFormatting.GREEN);
            client.player.displayClientMessage(Component.translatable("enhancedkeybinds.actionbar.sneaking_mode", mode), true);
        }

        while (toggleSprint != null && toggleSprint.consumeClick()) {
            boolean enabled = !client.options.toggleSprint().get();
            client.options.toggleSprint().set(enabled);
            MutableComponent mode = Component.translatable(enabled ? "enhancedkeybinds.actionbar.component.toggle" : "enhancedkeybinds.actionbar.component.hold")
                    .withStyle(ChatFormatting.GREEN);
            client.player.displayClientMessage(
                    Component.translatable("enhancedkeybinds.actionbar.sprinting_mode", mode), true);
        }

        while (toggleClosedCaptions != null && toggleClosedCaptions.consumeClick()) {
            boolean enabled = !client.options.showSubtitles().get();
            client.options.showSubtitles().set(enabled);
            client.player.displayClientMessage(stateMessage("enhancedkeybinds.actionbar.closed-captions", enabled), true);
        }
    }

    private static Component stateMessage(String messageKeyPrefix, boolean enabled) {
        return Component.translatable(
                "enhancedkeybinds.actionbar.component.toggled",
                Component.translatable(messageKeyPrefix),
                Component.translatable(enabled ? "enhancedkeybinds.actionbar.component.enabled" : "enhancedkeybinds.actionbar.component.disabled")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)
        );
    }

    private static KeyMapping register(String translationKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(translationKey, InputConstants.UNKNOWN.getValue(), RegisterCategories.CONTROLS));
    }
}