package me.coolaid.enhancedkeybinds.mixin;

import me.coolaid.enhancedkeybinds.screen.KeyBindsListEntryRefresher;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyBindsList.class)
public abstract class KeyBindsListMixin {
    @Inject(method = "refreshEntries", at = @At("HEAD"), cancellable = true)
    private void enhancedkeybinds$safeRefreshEntries(CallbackInfo ci) {
        KeyBindsList list = (KeyBindsList) (Object) this;
        list.children().forEach(KeyBindsListEntryRefresher::refresh);
        ci.cancel();
    }
}