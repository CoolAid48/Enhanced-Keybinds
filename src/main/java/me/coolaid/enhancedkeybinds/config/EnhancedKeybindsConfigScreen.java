package me.coolaid.enhancedkeybinds.config;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.controller.TickBoxControllerBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class EnhancedKeybindsConfigScreen {
    private EnhancedKeybindsConfigScreen() {
    }

    public static Screen create(Screen parent) {
        return YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("enhancedkeybinds.config.title"))
                .category(ConfigCategory.createBuilder()
                        .name(Component.translatable("enhancedkeybinds.config.category.general"))
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("enhancedkeybinds.config.option.register_controls_keybinds"))
                                .description(OptionDescription.of(Component.translatable("enhancedkeybinds.config.option.register_controls_keybinds.description")))
                                .binding(
                                        true,
                                        () -> EnhancedKeybindsConfig.data().registerControlsKeybinds,
                                        value -> EnhancedKeybindsConfig.data().registerControlsKeybinds = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("enhancedkeybinds.config.option.register_skin_layer_keybinds"))
                                .description(OptionDescription.of(Component.translatable("enhancedkeybinds.config.option.register_skin_layer_keybinds.description")))
                                .binding(
                                        true,
                                        () -> EnhancedKeybindsConfig.data().registerSkinLayerKeybinds,
                                        value -> EnhancedKeybindsConfig.data().registerSkinLayerKeybinds = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("enhancedkeybinds.config.option.include_individual_skin_layers"))
                                .description(OptionDescription.of(Component.translatable("enhancedkeybinds.config.option.include_individual_skin_layers.description")))
                                .binding(
                                        false,
                                        () -> EnhancedKeybindsConfig.data().includeIndividualSkinLayers,
                                        value -> EnhancedKeybindsConfig.data().includeIndividualSkinLayers = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .save(EnhancedKeybindsConfig::save)
                .build()
                .generateScreen(parent);
    }
}