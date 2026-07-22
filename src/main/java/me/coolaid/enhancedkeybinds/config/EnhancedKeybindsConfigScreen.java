package me.coolaid.enhancedkeybinds.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.OptionFlag;
import dev.isxander.yacl3.api.OptionGroup;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.Consumer;
import java.util.function.Supplier;

public final class EnhancedKeybindsConfigScreen {
    private EnhancedKeybindsConfigScreen() {
    }

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("enhancedkeybinds.config.title"))
                .category(accessibilityCategory())
                .category(skinCustomizationCategory())
                .save(EnhancedKeybindsConfig::save)
                .build()
                .generateScreen(parent);
    }

    private static ConfigCategory accessibilityCategory() {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("enhancedkeybinds.config.category.accessibility"))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_closed_captions_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerClosedCaptionsKeybind,
                        value -> EnhancedKeybindsConfig.data().registerClosedCaptionsKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_chat_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerChatKeybind,
                        value -> EnhancedKeybindsConfig.data().registerChatKeybind = value
                ))
                .group(controlsGroup())
                .build();
    }

    private static OptionGroup controlsGroup() {
        return OptionGroup.createBuilder()
                .name(Component.translatable("enhancedkeybinds.config.group.controls"))
                .collapsed(false)
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_auto_jump_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerAutoJumpKeybind,
                        value -> EnhancedKeybindsConfig.data().registerAutoJumpKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_sneak_mode_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerSneakModeKeybind,
                        value -> EnhancedKeybindsConfig.data().registerSneakModeKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_sprint_mode_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerSprintModeKeybind,
                        value -> EnhancedKeybindsConfig.data().registerSprintModeKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_attack_mode_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerAttackModeKeybind,
                        value -> EnhancedKeybindsConfig.data().registerAttackModeKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_use_mode_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerUseModeKeybind,
                        value -> EnhancedKeybindsConfig.data().registerUseModeKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_operator_items_tab_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerOperatorItemsTabKeybind,
                        value -> EnhancedKeybindsConfig.data().registerOperatorItemsTabKeybind = value
                ))
                .build();
    }

    private static ConfigCategory skinCustomizationCategory() {
        return ConfigCategory.createBuilder()
                .name(Component.translatable("enhancedkeybinds.config.category.skin_customization"))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_swap_main_hand_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerSwapMainHandKeybind,
                        value -> EnhancedKeybindsConfig.data().registerSwapMainHandKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_toggle_all_skin_layers_keybind",
                        true,
                        () -> EnhancedKeybindsConfig.data().registerToggleAllSkinLayersKeybind,
                        value -> EnhancedKeybindsConfig.data().registerToggleAllSkinLayersKeybind = value
                ))
                .group(individualSkinLayersGroup())
                .build();
    }

    private static OptionGroup individualSkinLayersGroup() {
        return OptionGroup.createBuilder()
                .name(Component.translatable("enhancedkeybinds.config.group.individual_skin_layers"))
                .collapsed(false)
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_toggle_cape_keybind",
                        false,
                        () -> EnhancedKeybindsConfig.data().registerToggleCapeKeybind,
                        value -> EnhancedKeybindsConfig.data().registerToggleCapeKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_toggle_hat_keybind",
                        false,
                        () -> EnhancedKeybindsConfig.data().registerToggleHatKeybind,
                        value -> EnhancedKeybindsConfig.data().registerToggleHatKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_toggle_jacket_keybind",
                        false,
                        () -> EnhancedKeybindsConfig.data().registerToggleJacketKeybind,
                        value -> EnhancedKeybindsConfig.data().registerToggleJacketKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_toggle_left_sleeve_keybind",
                        false,
                        () -> EnhancedKeybindsConfig.data().registerToggleLeftSleeveKeybind,
                        value -> EnhancedKeybindsConfig.data().registerToggleLeftSleeveKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_toggle_right_sleeve_keybind",
                        false,
                        () -> EnhancedKeybindsConfig.data().registerToggleRightSleeveKeybind,
                        value -> EnhancedKeybindsConfig.data().registerToggleRightSleeveKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_toggle_left_pants_keybind",
                        false,
                        () -> EnhancedKeybindsConfig.data().registerToggleLeftPantsKeybind,
                        value -> EnhancedKeybindsConfig.data().registerToggleLeftPantsKeybind = value
                ))
                .option(restartOption(
                        "enhancedkeybinds.config.option.register_toggle_right_pants_keybind",
                        false,
                        () -> EnhancedKeybindsConfig.data().registerToggleRightPantsKeybind,
                        value -> EnhancedKeybindsConfig.data().registerToggleRightPantsKeybind = value
                ))
                .build();
    }

    private static Option<Boolean> restartOption(String translationKey, boolean defaultValue, Supplier<Boolean> getter, Consumer<Boolean> setter) {
        return Option.<Boolean>createBuilder()
                .name(Component.translatable(translationKey))
                .description(OptionDescription.of(Component.translatable(translationKey + ".description")))
                .binding(defaultValue, getter, setter)
                .flag(OptionFlag.GAME_RESTART)
                .controller(TickBoxControllerBuilder::create)
                .build();
    }
}
