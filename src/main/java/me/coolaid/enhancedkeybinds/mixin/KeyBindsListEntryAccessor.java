package me.coolaid.enhancedkeybinds.mixin;

import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyBindsList.Entry.class)
public interface KeyBindsListEntryAccessor {
    @Invoker("refreshEntry")
    void enhancedkeybinds$refreshEntry();
}
