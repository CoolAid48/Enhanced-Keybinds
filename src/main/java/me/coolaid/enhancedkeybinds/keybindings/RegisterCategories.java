package me.coolaid.enhancedkeybinds.keybindings;

import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class RegisterCategories {
    public static final KeyMapping.Category CONTROLS = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("enhancedkeybinds", "controls")
    );

    public static final KeyMapping.Category SKIN_LAYERS = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("enhancedkeybinds", "skin_layers")
    );

    private RegisterCategories() {
    }
}