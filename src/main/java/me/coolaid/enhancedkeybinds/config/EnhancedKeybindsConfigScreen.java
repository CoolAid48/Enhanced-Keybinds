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
                                .name(Component.translatable("enhancedkeybinds.config.option.test1"))
                                .description(OptionDescription.of(Component.translatable("enhancedkeybinds.config.test1.description")))
                                .binding(
                                        true,
                                        () -> EnhancedKeybindsConfig.data().test1,
                                        value -> EnhancedKeybindsConfig.data().test1 = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .option(Option.<Boolean>createBuilder()
                                .name(Component.translatable("enhancedkeybinds.config.option.test2"))
                                .description(OptionDescription.of(Component.translatable("enhancedkeybinds.config.option.test2.description")))
                                .binding(
                                        true,
                                        () -> EnhancedKeybindsConfig.data().test2,
                                        value -> EnhancedKeybindsConfig.data().test2 = value
                                )
                                .controller(TickBoxControllerBuilder::create)
                                .build())
                        .build())
                .save(EnhancedKeybindsConfig::save)
                .build()
                .generateScreen(parent);
    }
}