package me.coolaid.enhancedkeybinds.mixin;

import me.coolaid.enhancedkeybinds.screen.KeyBindsFilterController;
import me.coolaid.enhancedkeybinds.screen.KeyBindsFilterMode;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsScreenMixin {
    @Unique
    private Button enhancedkeybinds$unboundFilterButton;

    @Unique
    private Button enhancedkeybinds$conflictsFilterButton;

    @Redirect(
            method = "addFooter",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;",
                    ordinal = 0
            ),
            require = 0
    )
    private Button.Builder enhancedkeybinds$replaceResetAllButton(Component message, Button.OnPress vanillaReset) {
        return KeyBindsFilterController.createResetAllButtonBuilder((KeyBindsScreen) (Object) this, vanillaReset);
    }

    @Redirect(
            method = "addFooter",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/layouts/HeaderAndFooterLayout;addToFooter(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;"
            ),
            require = 0
    )
    private LayoutElement enhancedkeybinds$wrapFooter(HeaderAndFooterLayout layout, LayoutElement vanillaFooterRow) {
        KeyBindsScreen screen = (KeyBindsScreen) (Object) this;

        LinearLayout filterRow = this.enhancedkeybinds$createFilterRow(screen);

        LinearLayout footer = LinearLayout.vertical().spacing(4);
        footer.addChild(filterRow);
        footer.addChild(vanillaFooterRow);

        layout.setFooterHeight(Math.max(layout.getFooterHeight(), 54));
        layout.addToFooter(footer);
        return vanillaFooterRow;
    }

    @Inject(method = "addFooter", at = @At("TAIL"))
    private void enhancedkeybinds$addFilterFallback(CallbackInfo ci) {
        if (this.enhancedkeybinds$unboundFilterButton != null) {
            return;
        }

        HeaderAndFooterLayout layout = ((OptionsSubScreen) (Object) this).layout;
        layout.setFooterHeight(Math.max(layout.getFooterHeight(), 64));
        layout.addToFooter(
                this.enhancedkeybinds$createFilterRow((KeyBindsScreen) (Object) this),
                settings -> settings.align(0.5F, 0.0F)
        );
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void enhancedkeybinds$handleSearchShortcut(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (KeyBindsFilterController.handleSearchKeyPressed((KeyBindsScreen) (Object) this, event)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "extractRenderState", at = @At("HEAD"))
    private void enhancedkeybinds$syncFilters(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        KeyBindsFilterController.sync((KeyBindsScreen) (Object) this);
    }

    @Unique
    private LinearLayout enhancedkeybinds$createFilterRow(KeyBindsScreen screen) {
        this.enhancedkeybinds$unboundFilterButton = KeyBindsFilterController.createButton(screen, KeyBindsFilterMode.UNBOUND);
        this.enhancedkeybinds$conflictsFilterButton = KeyBindsFilterController.createButton(screen, KeyBindsFilterMode.CONFLICTS);

        LinearLayout filterRow = LinearLayout.horizontal().spacing(8);
        filterRow.addChild(this.enhancedkeybinds$unboundFilterButton);
        filterRow.addChild(this.enhancedkeybinds$conflictsFilterButton);
        KeyBindsFilterController.attachButtons(screen, this.enhancedkeybinds$unboundFilterButton, this.enhancedkeybinds$conflictsFilterButton);
        return filterRow;
    }
}
