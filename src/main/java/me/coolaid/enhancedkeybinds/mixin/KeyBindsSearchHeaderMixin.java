package me.coolaid.enhancedkeybinds.mixin;

import me.coolaid.enhancedkeybinds.screen.KeyBindsFilterController;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.layouts.HeaderAndFooterLayout;
import net.minecraft.client.gui.layouts.LayoutSettings;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.options.OptionsSubScreen;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OptionsSubScreen.class)
public abstract class KeyBindsSearchHeaderMixin extends Screen {
    @Unique
    private static final int SEARCH_WIDTH = 200;

    @Shadow
    @Final
    public HeaderAndFooterLayout layout;

    protected KeyBindsSearchHeaderMixin(Component title) {
        super(title);
    }

    @Inject(method = "addTitle", at = @At("HEAD"), cancellable = true)
    private void enhancedkeybinds$addSearchHeader(CallbackInfo ci) {
        if (!((Object) this instanceof KeyBindsScreen screen)) {
            return;
        }

        this.layout.setHeaderHeight(48);
        int centerX = this.width / 2;
        Component searchLabel = Component.translatable("enhancedkeybinds.keybind_search");
        EditBox searchBox = new EditBox(
                this.font,
                centerX - SEARCH_WIDTH / 2,
                20,
                SEARCH_WIDTH,
                Button.DEFAULT_HEIGHT,
                searchLabel
        );
        searchBox.setHint(searchLabel.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        KeyBindsFilterController.attachSearchBox(screen, searchBox);

        LinearLayout header = this.layout.addToHeader(
                LinearLayout.vertical(),
                settings -> settings.paddingVertical(8)
        );
        header.addChild(new StringWidget(this.title, this.font), LayoutSettings::alignHorizontallyCenter);
        header.addChild(searchBox, settings -> settings.paddingVertical(4));
        this.setInitialFocus(searchBox);
        ci.cancel();
    }
}
