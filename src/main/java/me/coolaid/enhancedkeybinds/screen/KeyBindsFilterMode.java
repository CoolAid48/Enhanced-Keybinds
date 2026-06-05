package me.coolaid.enhancedkeybinds.screen;

public enum KeyBindsFilterMode {
    ALL(""),
    UNBOUND("enhancedkeybinds.keybind_filter.unbound"),
    CONFLICTS("enhancedkeybinds.keybind_filter.conflicts");

    private final String translationKey;

    KeyBindsFilterMode(String translationKey) {
        this.translationKey = translationKey;
    }

    public String translationKey() {
        return translationKey;
    }
}
