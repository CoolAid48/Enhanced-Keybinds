package me.coolaid.enhancedkeybinds.keybindings;

import me.coolaid.enhancedkeybinds.EnhancedKeybinds;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;

public final class RegisterCategories {
    public static final KeyMapping.Category CONTROLS = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(EnhancedKeybinds.MOD_ID, "controls")
    );

    public static final KeyMapping.Category SKIN_CUSTOMIZATION = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(EnhancedKeybinds.MOD_ID, "skin_customization")
    );

    private RegisterCategories() {
    }
}
