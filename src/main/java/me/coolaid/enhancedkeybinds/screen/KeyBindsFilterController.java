package me.coolaid.enhancedkeybinds.screen;

import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import me.coolaid.enhancedkeybinds.mixin.KeyBindsScreenAccessor;
import me.coolaid.enhancedkeybinds.mixin.KeyEntryAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;

public final class KeyBindsFilterController {
    private static final int BUTTON_WIDTH = 150;
    private static final Map<KeyBindsScreen, FilterState> STATES = new WeakHashMap<>();
    private static final Map<Class<?>, Optional<AccessibleObject>> KEY_ACCESSORS = new HashMap<>();
    private static final Map<Class<?>, Boolean> CATEGORY_ENTRY_CLASSES = new HashMap<>();

    private KeyBindsFilterController() {
    }

    public static Button createButton(KeyBindsScreen screen, KeyBindsFilterMode mode) {
        return Button.builder(buttonMessage(mode, false), pressed -> toggle(screen, mode))
                .width(BUTTON_WIDTH)
                .build();
    }

    public static void attachSearchBox(KeyBindsScreen screen, EditBox searchBox) {
        FilterState state = state(screen);
        state.searchBox = searchBox;
        searchBox.setValue(state.searchQuery);
        searchBox.setResponder(query -> updateSearch(screen, query));
    }

    public static boolean handleSearchKeyPressed(KeyBindsScreen screen, KeyEvent event) {
        FilterState state = STATES.get(screen);
        if (state == null || state.searchBox == null) {
            return false;
        }

        EditBox searchBox = state.searchBox;
        if (!searchBox.isFocused() && screen.selectedKey == null
                && event.hasControlDown() && event.key() == GLFW.GLFW_KEY_F) {
            screen.setFocused(searchBox);
            return true;
        }
        if (searchBox.isFocused() && event.isEscape()) {
            screen.setFocused(null);
            return true;
        }
        return false;
    }

    public static Button.Builder createResetAllButtonBuilder(KeyBindsScreen screen, Button.OnPress vanillaReset) {
        return Button.builder(Component.translatable("enhancedkeybinds.keybind_filter.reset_all"), button -> {
            FilterState state = state(screen);
            state.resetButton = button;

            if (!state.confirmingReset) {
                state.confirmingReset = true;
                button.setMessage(Component.translatable("enhancedkeybinds.keybind_filter.confirm_reset"));
                return;
            }

            state.confirmingReset = false;
            vanillaReset.onPress(button);
            resetAmecsModifiers();
            refreshList(screen);
            state.filterDirty = true;
            button.setMessage(Component.translatable("enhancedkeybinds.keybind_filter.reset_all"));
        }).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT);
    }

    public static void attachButtons(KeyBindsScreen screen, Button unboundButton, Button conflictsButton) {
        FilterState state = state(screen);
        state.unboundButton = unboundButton;
        state.conflictsButton = conflictsButton;
        KeyBindsList list = keyBindsList(screen);
        if (list != null && state.list != list) {
            captureSourceEntries(state, list, entries(list));
        }
        updateButtons(state);
    }

    public static void toggle(KeyBindsScreen screen, KeyBindsFilterMode selectedMode) {
        KeyBindsList list = keyBindsList(screen);
        if (list == null) {
            return;
        }

        FilterState state = state(screen);
        List<KeyBindsList.Entry> currentEntries = entries(list);
        if (state.list != list || state.sourceEntries == null || state.visibleEntries == null
                || !currentEntries.equals(state.visibleEntries)) {
            captureSourceEntries(state, list, currentEntries);
        }

        state.mode = state.mode == selectedMode ? KeyBindsFilterMode.ALL : selectedMode;
        state.filterDirty = true;
        applyFilter(list, state, true, currentEntries, keyStateFingerprint());
        updateButtons(state);
    }

    private static void updateSearch(KeyBindsScreen screen, String query) {
        FilterState state = state(screen);
        String normalizedQuery = normalizeSearchText(query);
        if (normalizedQuery.equals(state.searchQuery)) {
            return;
        }

        state.searchQuery = normalizedQuery;
        state.filterDirty = true;

        KeyBindsList list = keyBindsList(screen);
        if (list == null) {
            return;
        }

        List<KeyBindsList.Entry> currentEntries = entries(list);
        if (state.list != list || state.sourceEntries == null || state.visibleEntries == null
                || !currentEntries.equals(state.visibleEntries)) {
            captureSourceEntries(state, list, currentEntries);
        }
        applyFilter(list, state, true, currentEntries, keyStateFingerprint());
    }

    public static void sync(KeyBindsScreen screen) {
        FilterState state = STATES.get(screen);
        if (state == null) {
            return;
        }

        syncResetButton(state);

        if (state.mode == KeyBindsFilterMode.ALL && state.searchQuery.isEmpty() && !state.filterDirty) {
            return;
        }

        KeyBindsList list = keyBindsList(screen);
        if (list == null) {
            return;
        }

        List<KeyBindsList.Entry> currentEntries = entries(list);
        long keyStateFingerprint = keyStateFingerprint();
        if (state.list != list || state.sourceEntries == null || state.visibleEntries == null) {
            captureSourceEntries(state, list, currentEntries);
            state.filterDirty = true;
            applyFilter(list, state, true, currentEntries, keyStateFingerprint);
            return;
        }

        if (!currentEntries.equals(state.visibleEntries)) {
            captureSourceEntries(state, list, currentEntries);
            state.filterDirty = true;
        }

        if (state.filterDirty || state.keyStateFingerprint != keyStateFingerprint) {
            applyFilter(list, state, false, currentEntries, keyStateFingerprint);
        }
    }

    private static void applyFilter(
            KeyBindsList list,
            FilterState state,
            boolean resetScroll,
            List<KeyBindsList.Entry> currentEntries,
            long keyStateFingerprint
    ) {
        if (state.sourceEntries == null) {
            captureSourceEntries(state, list, currentEntries);
        }

        List<KeyBindsList.Entry> filteredEntries = filterEntries(state.sourceEntries, state.mode, state.searchQuery);
        if (filteredEntries.equals(currentEntries)) {
            if (resetScroll) {
                resetScroll(list);
            }
            state.visibleEntries = filteredEntries;
            state.keyStateFingerprint = keyStateFingerprint;
            state.filterDirty = false;
            return;
        }

        replaceEntries(list, filteredEntries, resetScroll);
        state.visibleEntries = filteredEntries;
        state.keyStateFingerprint = keyStateFingerprint;
        state.filterDirty = false;
    }

    private static void captureSourceEntries(FilterState state, KeyBindsList list, List<KeyBindsList.Entry> sourceEntries) {
        state.list = list;
        state.sourceEntries = List.copyOf(sourceEntries);
        state.visibleEntries = state.sourceEntries;
    }

    private static void replaceEntries(KeyBindsList list, List<KeyBindsList.Entry> newEntries, boolean resetScroll) {
        if (resetScroll) {
            resetScroll(list);
        }
        list.replaceEntries(newEntries);
        list.refreshEntries();
        if (resetScroll) {
            resetScroll(list);
        }
    }

    private static void resetScroll(KeyBindsList list) {
        list.setScrollAmount(0);
    }

    private static List<KeyBindsList.Entry> filterEntries(
            List<KeyBindsList.Entry> sourceEntries,
            KeyBindsFilterMode mode,
            String searchQuery
    ) {
        if (mode == KeyBindsFilterMode.ALL && searchQuery.isEmpty()) {
            return sourceEntries;
        }

        List<KeyBindsList.Entry> filteredEntries = new ArrayList<>();
        Set<KeyMapping> conflictingKeys = mode == KeyBindsFilterMode.CONFLICTS ? findConflictingKeys() : Set.of();
        KeyBindsList.Entry currentCategory = null;
        boolean categoryAdded = false;

        for (KeyBindsList.Entry entry : sourceEntries) {
            KeyMapping key = key(entry);
            if (key == null) {
                if (isCategory(entry)) {
                    currentCategory = entry;
                    categoryAdded = false;
                }
                continue;
            }

            if (matches(key, mode, conflictingKeys) && matchesSearch(key, searchQuery)) {
                if (currentCategory != null && !categoryAdded) {
                    filteredEntries.add(currentCategory);
                    categoryAdded = true;
                }
                filteredEntries.add(entry);
            }
        }

        return filteredEntries;
    }

    private static boolean matches(KeyMapping key, KeyBindsFilterMode mode, Set<KeyMapping> conflictingKeys) {
        return switch (mode) {
            case ALL -> true;
            case UNBOUND -> key.isUnbound();
            case CONFLICTS -> conflictingKeys.contains(key);
        };
    }

    private static boolean matchesSearch(KeyMapping key, String query) {
        if (query.isEmpty()) {
            return true;
        }
        if (containsNormalized(key.getCategory().label().getString(), query)) {
            return true;
        }
        if (containsNormalized(Component.translatable(key.getName()).getString(), query)) {
            return true;
        }
        return !key.isUnbound() && containsNormalized(key.getTranslatedKeyMessage().getString(), query);
    }

    private static String normalizeSearchText(String text) {
        return text == null || text.isBlank() ? "" : text.toLowerCase(Locale.ROOT);
    }

    private static boolean containsNormalized(String candidate, String query) {
        return candidate != null && candidate.toLowerCase(Locale.ROOT).contains(query);
    }

    private static Set<KeyMapping> findConflictingKeys() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return Set.of();
        }

        KeyMapping[] keyMappings = minecraft.options.keyMappings;
        Set<KeyMapping> conflicts = Collections.newSetFromMap(new IdentityHashMap<>());
        for (int firstIndex = 0; firstIndex < keyMappings.length; firstIndex++) {
            KeyMapping first = keyMappings[firstIndex];
            if (first.isUnbound()) {
                continue;
            }

            for (int secondIndex = firstIndex + 1; secondIndex < keyMappings.length; secondIndex++) {
                KeyMapping second = keyMappings[secondIndex];
                if (!second.isUnbound() && first.same(second) && (!first.isDefault() || !second.isDefault())) {
                    conflicts.add(first);
                    conflicts.add(second);
                }
            }
        }
        return conflicts;
    }

    private static KeyMapping key(KeyBindsList.Entry entry) {
        if (entry instanceof KeyEntryAccessor accessor) {
            return accessor.enhancedkeybinds$getKey();
        }

        Optional<AccessibleObject> accessor = KEY_ACCESSORS.computeIfAbsent(entry.getClass(), KeyBindsFilterController::findKeyAccessor);
        if (accessor.isEmpty()) {
            return null;
        }

        try {
            AccessibleObject object = accessor.get();
            if (object instanceof Method method) {
                Object value = method.invoke(entry);
                return value instanceof KeyMapping key ? key : null;
            }
            if (object instanceof Field field) {
                Object value = field.get(entry);
                return value instanceof KeyMapping key ? key : null;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private static Optional<AccessibleObject> findKeyAccessor(Class<?> entryClass) {
        for (String methodName : List.of("getKey", "getKeyMapping", "getKeyBinding", "amecs$getKeyBinding")) {
            Method method = findNoArgMethod(entryClass, methodName);
            if (method != null && KeyMapping.class.isAssignableFrom(method.getReturnType())) {
                method.setAccessible(true);
                return Optional.of(method);
            }
        }

        for (String fieldName : List.of("key", "keyMapping", "keyBinding", "mapping")) {
            Field field = findField(entryClass, fieldName);
            if (field != null && KeyMapping.class.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return Optional.of(field);
            }
        }

        return Optional.empty();
    }

    private static Method findNoArgMethod(Class<?> entryClass, String methodName) {
        Class<?> currentClass = entryClass;
        while (currentClass != null) {
            try {
                Method method = currentClass.getDeclaredMethod(methodName);
                if (method.getParameterCount() == 0) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    private static Field findField(Class<?> entryClass, String fieldName) {
        Class<?> currentClass = entryClass;
        while (currentClass != null) {
            try {
                return currentClass.getDeclaredField(fieldName);
            } catch (NoSuchFieldException ignored) {
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
    }

    private static boolean isCategory(KeyBindsList.Entry entry) {
        return CATEGORY_ENTRY_CLASSES.computeIfAbsent(entry.getClass(), KeyBindsFilterController::isCategoryClass);
    }

    private static boolean isCategoryClass(Class<?> entryClass) {
        if (KeyBindsList.CategoryEntry.class.isAssignableFrom(entryClass)) {
            return true;
        }
        String className = entryClass.getSimpleName().toLowerCase(Locale.ROOT);
        return className.contains("category") || findNoArgMethod(entryClass, "category") != null || findField(entryClass, "category") != null;
    }

    private static KeyBindsList keyBindsList(KeyBindsScreen screen) {
        try {
            return ((KeyBindsScreenAccessor) screen).enhancedkeybinds$getKeyBindsList();
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    private static List<KeyBindsList.Entry> entries(KeyBindsList list) {
        return List.copyOf(list.children());
    }


    private static FilterState state(KeyBindsScreen screen) {
        return STATES.computeIfAbsent(screen, ignored -> new FilterState());
    }

    private static void updateButtons(FilterState state) {
        if (state.unboundButton != null) {
            state.unboundButton.setMessage(buttonMessage(KeyBindsFilterMode.UNBOUND, state.mode == KeyBindsFilterMode.UNBOUND));
        }
        if (state.conflictsButton != null) {
            state.conflictsButton.setMessage(buttonMessage(KeyBindsFilterMode.CONFLICTS, state.mode == KeyBindsFilterMode.CONFLICTS));
        }
    }

    private static Component buttonMessage(KeyBindsFilterMode mode, boolean active) {
        if (!active) {
            return Component.translatable(mode.translationKey());
        }
        return Component.translatable("enhancedkeybinds.keybind_filter.active");
    }

    private static void syncResetButton(FilterState state) {
        if (state.resetButton == null || state.resetButton.active) {
            return;
        }

        state.confirmingReset = false;
        state.resetButton.setMessage(Component.translatable("enhancedkeybinds.keybind_filter.reset_all"));
    }

    private static void resetAmecsModifiers() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return;
        }

        for (KeyMapping keyMapping : minecraft.options.keyMappings) {
            AmecsKeyModifiersApi.resetBoundModifiers(keyMapping);
        }
        KeyMapping.resetMapping();
    }

    private static void refreshList(KeyBindsScreen screen) {
        KeyBindsList list = keyBindsList(screen);
        if (list != null) {
            list.resetMappingAndUpdateButtons();
        }
    }

    private static long keyStateFingerprint() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return 0L;
        }

        long fingerprint = 1125899906842597L;
        for (KeyMapping keyMapping : minecraft.options.keyMappings) {
            fingerprint = 31L * fingerprint + keyMapping.saveString().hashCode();
            fingerprint = 31L * fingerprint + Boolean.hashCode(keyMapping.isDefault());
            fingerprint = 31L * fingerprint + keyMapping.getTranslatedKeyMessage().getString().hashCode();
        }
        return fingerprint;
    }

    private static final class FilterState {
        private KeyBindsFilterMode mode = KeyBindsFilterMode.ALL;
        private KeyBindsList list;
        private List<KeyBindsList.Entry> sourceEntries;
        private List<KeyBindsList.Entry> visibleEntries;
        private String searchQuery = "";
        private EditBox searchBox;
        private long keyStateFingerprint;
        private boolean filterDirty;
        private Button unboundButton;
        private Button conflictsButton;
        private Button resetButton;
        private boolean confirmingReset;
    }

}
