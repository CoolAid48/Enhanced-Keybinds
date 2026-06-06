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
import net.minecraft.world.entity.player.ChatVisiblity;

public final class ControlsTogglesBinds {
    private static KeyMapping toggleAutoJump;
    private static KeyMapping toggleSneak;
    private static KeyMapping toggleSprint;
    private static KeyMapping toggleClosedCaptions;
    private static KeyMapping toggleAttackMode;
    private static KeyMapping toggleUseMode;
    private static KeyMapping toggleOperatorItemsTab;
    private static KeyMapping cycleChat;

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
        if (EnhancedKeybindsConfig.data().registerAttackModeKeybind) {
            toggleAttackMode = register("key.enhancedkeybinds.toggle_attack_mode");
        }
        if (EnhancedKeybindsConfig.data().registerUseModeKeybind) {
            toggleUseMode = register("key.enhancedkeybinds.toggle_use_mode");
        }
        if (EnhancedKeybindsConfig.data().registerOperatorItemsTabKeybind) {
            toggleOperatorItemsTab = register("key.enhancedkeybinds.toggle_operator_items_tab");
        }
        if (EnhancedKeybindsConfig.data().registerChatKeybind) {
            cycleChat = register("key.enhancedkeybinds.chat");
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

        while (toggleAttackMode != null && toggleAttackMode.consumeClick()) {
            boolean enabled = !client.options.toggleAttack().get();
            client.options.toggleAttack().set(enabled);
            if (!enabled) {
                cancelAttackAction(client);
            }
            client.options.save();
            client.player.displayClientMessage(modeMessage("enhancedkeybinds.actionbar.attack-destroy", enabled), true);
        }

        while (toggleUseMode != null && toggleUseMode.consumeClick()) {
            boolean enabled = !client.options.toggleUse().get();
            client.options.toggleUse().set(enabled);
            if (!enabled) {
                cancelUseAction(client);
            }
            client.options.save();
            client.player.displayClientMessage(modeMessage("enhancedkeybinds.actionbar.use-item-place-block", enabled), true);
        }

        while (toggleOperatorItemsTab != null && toggleOperatorItemsTab.consumeClick()) {
            boolean enabled = !client.options.operatorItemsTab().get();
            client.options.operatorItemsTab().set(enabled);
            client.options.save();
            client.player.displayClientMessage(stateMessage("enhancedkeybinds.actionbar.operator-items-tab", enabled), true);
        }

        while (cycleChat != null && cycleChat.consumeClick()) {
            ChatVisiblity visibility = nextChatVisibility(client.options.chatVisibility().get());
            client.options.chatVisibility().set(visibility);
            client.options.save();
            client.player.displayClientMessage(
                    Component.translatable("enhancedkeybinds.actionbar.chat", chatVisibilityMode(visibility)), true);
        }
    }

    private static Component modeMessage(String messageKeyPrefix, boolean toggleEnabled) {
        MutableComponent mode = Component.translatable(toggleEnabled ? "enhancedkeybinds.actionbar.component.toggle" : "enhancedkeybinds.actionbar.component.hold")
                .withStyle(ChatFormatting.GREEN);
        return Component.translatable(messageKeyPrefix, mode);
    }

    private static Component stateMessage(String messageKeyPrefix, boolean enabled) {
        return Component.translatable(
                "enhancedkeybinds.actionbar.component.toggled",
                Component.translatable(messageKeyPrefix),
                Component.translatable(enabled ? "enhancedkeybinds.actionbar.component.enabled" : "enhancedkeybinds.actionbar.component.disabled")
                        .withStyle(enabled ? ChatFormatting.GREEN : ChatFormatting.RED)
        );
    }

    private static void cancelAttackAction(Minecraft client) {
        client.options.keyAttack.setDown(false);
        if (client.gameMode != null && client.gameMode.isDestroying()) {
            client.gameMode.stopDestroyBlock();
        }
    }

    private static void cancelUseAction(Minecraft client) {
        client.options.keyUse.setDown(false);
        if (client.gameMode != null && client.player.isUsingItem()) {
            client.gameMode.releaseUsingItem(client.player);
        }
    }

    private static ChatVisiblity nextChatVisibility(ChatVisiblity visibility) {
        if (visibility == ChatVisiblity.HIDDEN) {
            return ChatVisiblity.SYSTEM;
        }
        if (visibility == ChatVisiblity.SYSTEM) {
            return ChatVisiblity.FULL;
        }
        return ChatVisiblity.HIDDEN;
    }

    private static MutableComponent chatVisibilityMode(ChatVisiblity visibility) {
        ChatFormatting color = switch (visibility) {
            case HIDDEN -> ChatFormatting.RED;
            case SYSTEM -> ChatFormatting.YELLOW;
            case FULL -> ChatFormatting.GREEN;
        };

        MutableComponent mode = visibility == ChatVisiblity.FULL
                ? Component.translatable("enhancedkeybinds.actionbar.component.shown")
                : visibility.caption().copy();
        return mode.withStyle(color);
    }

    private static KeyMapping register(String translationKey) {
        return KeyBindingHelper.registerKeyBinding(new KeyMapping(translationKey, InputConstants.UNKNOWN.getValue(), RegisterCategories.CONTROLS));
    }
}