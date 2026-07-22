package me.coolaid.enhancedkeybinds.mixin.controlling;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import de.siphalor.amecs.key_modifiers.impl.KeyBindingEditGuiHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "com.blamejared.controlling.client.NewKeyBindsScreen", remap = false)
public abstract class ControllingKeyBindsScreenMixin extends KeyBindsScreen {
    public ControllingKeyBindsScreenMixin(Screen lastScreen, Options options) {
        super(lastScreen, options);
    }

    @Shadow(remap = false)
    public abstract KeyBindsList getKeyBindsList();

    @Shadow(remap = false)
    public abstract Button resetButton();

    @Inject(method = {"keyPressed", "method_25404"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void enhancedkeybinds$handleAmecsKeyPress(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        KeyMapping selectedKey = this.selectedKey;
        if (selectedKey == null) {
            return;
        }

        if (event.isEscape()) {
            selectedKey.setKey(InputConstants.UNKNOWN);
            this.selectedKey = null;
        } else {
            InputConstants.Key key = InputConstants.getKey(event);
            KeyBindingEditGuiHelper.handleKeyPress(selectedKey, key);
            if (AmecsKeyModifiers.fromKey(key) == null) {
                this.selectedKey = null;
            }
        }

        this.lastKeySelection = Util.getMillis();
        this.getKeyBindsList().resetMappingAndUpdateButtons();
        cir.setReturnValue(true);
    }

    @Inject(method = {"mouseClicked", "method_25402"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void enhancedkeybinds$handleAmecsMouseClick(MouseButtonEvent event, boolean doubleClick, CallbackInfoReturnable<Boolean> cir) {
        Button resetButton = this.resetButton();
        if (resetButton != null && resetButton.active
                && resetButton.isMouseOver(event.x(), event.y()) && isConfirmingReset(resetButton)) {
            for (KeyMapping keyMapping : this.options.keyMappings) {
                AmecsKeyModifiersApi.resetBoundModifiers(keyMapping);
            }
        }

        KeyMapping selectedKey = this.selectedKey;
        if (selectedKey == null) {
            return;
        }

        KeyBindingEditGuiHelper.handleKeyPress(selectedKey, InputConstants.Type.MOUSE.getOrCreate(event.button()));
        this.selectedKey = null;
        this.lastKeySelection = Util.getMillis();
        this.getKeyBindsList().resetMappingAndUpdateButtons();
        cir.setReturnValue(true);
    }

    private static boolean isConfirmingReset(Button button) {
        return button.getMessage().getContents() instanceof TranslatableContents contents
                && "options.confirmReset".equals(contents.getKey());
    }

}
