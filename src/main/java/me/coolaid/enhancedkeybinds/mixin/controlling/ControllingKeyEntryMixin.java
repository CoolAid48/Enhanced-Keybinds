package me.coolaid.enhancedkeybinds.mixin.controlling;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.blamejared.controlling.client.NewKeyBindsList$KeyEntry", remap = false)
public abstract class ControllingKeyEntryMixin {
    @Shadow(remap = false)
    public abstract KeyMapping getKey();

    @Shadow(remap = false)
    public abstract Button getBtnChangeKeyBinding();

    @Shadow(remap = false)
    public abstract Button getBtnResetKeyBinding();

    @Inject(method = {"mouseClicked", "method_25402"}, at = @At("HEAD"), require = 0)
    private void enhancedkeybinds$syncAmecsStateBeforeClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        Button changeButton = this.getBtnChangeKeyBinding();
        if (changeButton.isMouseOver(event.x(), event.y())) {
            this.getKey().setKey(InputConstants.UNKNOWN);
        }

        Button resetButton = this.getBtnResetKeyBinding();
        if (resetButton.isMouseOver(event.x(), event.y())) {
            AmecsKeyModifiersApi.resetBoundModifiers(this.getKey());
        }
    }

}
