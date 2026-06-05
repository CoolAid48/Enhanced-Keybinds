package me.coolaid.enhancedkeybinds.screen;

import me.coolaid.enhancedkeybinds.mixin.KeyBindsScreenAccessor;
import me.coolaid.enhancedkeybinds.mixin.KeyEntryAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.client.gui.screens.options.controls.KeyBindsScreen;
import net.minecraft.network.chat.Component;

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
    private static final Map<Class<?>, Optional<AccessibleObject>> CONFLICT_ACCESSORS = new HashMap<>();
    private static final Map<Class<?>, Optional<Method>> ALL_ENTRY_ACCESSORS = new HashMap<>();
    private static final Map<Class<?>, Optional<EntryListMutators>> ENTRY_LIST_MUTATORS = new HashMap<>();
    private static final Map<Class<?>, Optional<Field>> SEARCH_FIELDS = new HashMap<>();
    private static final Map<Class<?>, Optional<Method>> SEARCH_VALUE_ACCESSORS = new HashMap<>();
    private static final Map<Class<?>, Boolean> CATEGORY_ENTRY_CLASSES = new HashMap<>();

    private KeyBindsFilterController() {
    }

    public static Button createButton(KeyBindsScreen screen, KeyBindsFilterMode mode) {
        return Button.builder(buttonMessage(mode, false), pressed -> toggle(screen, mode))
                .width(BUTTON_WIDTH)
                .build();
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
            resetAmecsModifiers(screen);
            refreshList(screen);
            state.filterDirty = true;
            button.setMessage(Component.translatable("enhancedkeybinds.keybind_filter.reset_all"));
        }).size(BUTTON_WIDTH, Button.DEFAULT_HEIGHT);
    }

    public static void attachButtons(KeyBindsScreen screen, Button unboundButton, Button conflictsButton) {
        FilterState state = state(screen);
        state.unboundButton = unboundButton;
        state.conflictsButton = conflictsButton;
        updateButtons(state);
    }

    public static void toggle(KeyBindsScreen screen, KeyBindsFilterMode selectedMode) {
        KeyBindsList list = keyBindsList(screen);
        if (list == null) {
            return;
        }

        FilterState state = state(screen);
        state.list = list;
        String searchQuery = searchQuery(screen);
        if (state.mode == selectedMode) {
            restoreAll(list, state, true);
            state.mode = KeyBindsFilterMode.ALL;
            state.filterDirty = false;
            state.searchQuery = searchQuery;
            updateButtons(state);
            return;
        }

        List<KeyBindsList.Entry> currentEntries = entries(list);
        List<KeyBindsList.Entry> sourceEntries = sourceEntries(list, currentEntries, searchQuery);
        if (state.mode == KeyBindsFilterMode.ALL || state.sourceEntries == null) {
            state.sourceEntries = sourceEntries;
        } else if (!currentEntries.equals(state.visibleEntries)) {
            state.sourceEntries = sourceEntries;
        }

        state.mode = selectedMode;
        state.searchQuery = searchQuery;
        state.filterDirty = true;
        applyFilter(list, state, true, currentEntries, keyStateFingerprint());
        updateButtons(state);
    }

    public static void sync(KeyBindsScreen screen) {
        FilterState state = STATES.get(screen);
        if (state == null) {
            return;
        }

        syncResetButton(state);

        if (state.mode == KeyBindsFilterMode.ALL) {
            return;
        }

        KeyBindsList list = keyBindsList(screen);
        if (list == null) {
            return;
        }

        List<KeyBindsList.Entry> currentEntries = entries(list);
        String searchQuery = searchQuery(screen);
        long keyStateFingerprint = keyStateFingerprint();
        if (state.list != list || !currentEntries.equals(state.visibleEntries) || !searchQuery.equals(state.searchQuery)) {
            state.list = list;
            state.searchQuery = searchQuery;
            state.sourceEntries = sourceEntries(list, currentEntries, searchQuery);
            state.filterDirty = true;
            applyFilter(list, state, true, currentEntries, keyStateFingerprint);
            return;
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
            state.sourceEntries = currentEntries;
        }

        List<KeyBindsList.Entry> filteredEntries = filterEntries(state.sourceEntries, state.mode);
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

    private static void restoreAll(KeyBindsList list, FilterState state, boolean resetScroll) {
        if (state.sourceEntries != null) {
            replaceEntries(list, state.sourceEntries, resetScroll);
        }
        state.sourceEntries = null;
        state.visibleEntries = null;
        state.keyStateFingerprint = keyStateFingerprint();
    }

    private static void replaceEntries(KeyBindsList list, List<KeyBindsList.Entry> newEntries, boolean resetScroll) {
        if (resetScroll) {
            resetScroll(list);
        }
        // More Search Bars keeps a separate full backing list; avoid mutating it when filtering visible rows.
        if (!replaceVisibleEntries(list, newEntries)) {
            list.replaceEntries(newEntries);
        }
        list.refreshEntries();
        if (resetScroll) {
            resetScroll(list);
        }
    }

    private static void resetScroll(KeyBindsList list) {
        list.setScrollAmount(0);
    }

    private static List<KeyBindsList.Entry> filterEntries(List<KeyBindsList.Entry> sourceEntries, KeyBindsFilterMode mode) {
        if (mode == KeyBindsFilterMode.ALL) {
            return new ArrayList<>(sourceEntries);
        }

        List<KeyBindsList.Entry> filteredEntries = new ArrayList<>();
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

            if (matches(entry, key, mode)) {
                if (currentCategory != null && !categoryAdded) {
                    filteredEntries.add(currentCategory);
                    categoryAdded = true;
                }
                filteredEntries.add(entry);
            }
        }

        return filteredEntries;
    }

    private static boolean matches(KeyBindsList.Entry entry, KeyMapping key, KeyBindsFilterMode mode) {
        return switch (mode) {
            case ALL -> true;
            case UNBOUND -> key.isUnbound();
            case CONFLICTS -> hasConflict(entry, key);
        };
    }

    private static boolean hasConflict(KeyBindsList.Entry entry, KeyMapping key) {
        if (key.isUnbound()) {
            return false;
        }

        KeyBindsListEntryRefresher.refresh(entry);

        Boolean entryConflict = entryConflict(entry);
        if (entryConflict != null) {
            return entryConflict;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return false;
        }

        for (KeyMapping other : minecraft.options.keyMappings) {
            if (other != key && !other.isUnbound() && key.same(other) && (!other.isDefault() || !key.isDefault())) {
                return true;
            }
        }
        return false;
    }

    private static Boolean entryConflict(KeyBindsList.Entry entry) {
        Optional<AccessibleObject> accessor = CONFLICT_ACCESSORS.computeIfAbsent(entry.getClass(), KeyBindsFilterController::findConflictAccessor);
        if (accessor.isEmpty()) {
            return null;
        }

        try {
            AccessibleObject object = accessor.get();
            if (object instanceof Method method) {
                Object value = method.invoke(entry);
                return value instanceof Boolean conflict ? conflict : null;
            }
            if (object instanceof Field field) {
                Object value = field.get(entry);
                return value instanceof Boolean conflict ? conflict : null;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }

    private static Optional<AccessibleObject> findConflictAccessor(Class<?> entryClass) {
        for (String methodName : List.of("hasCollision", "hasConflict", "isConflicting", "isConflict")) {
            Method method = findNoArgMethod(entryClass, methodName);
            if (method != null && (method.getReturnType() == boolean.class || method.getReturnType() == Boolean.class)) {
                method.setAccessible(true);
                return Optional.of(method);
            }
        }

        for (String fieldName : List.of("hasCollision", "conflicting", "hasConflict", "conflict")) {
            Field field = findField(entryClass, fieldName);
            if (field != null && (field.getType() == boolean.class || field.getType() == Boolean.class)) {
                field.setAccessible(true);
                return Optional.of(field);
            }
        }

        return Optional.empty();
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
        return new ArrayList<>(list.children());
    }

    private static List<KeyBindsList.Entry> sourceEntries(KeyBindsList list, List<KeyBindsList.Entry> fallbackEntries, String searchQuery) {
        // More Search Bars has already narrowed the visible list while search text is present.
        if (!searchQuery.isBlank()) {
            return fallbackEntries;
        }

        Optional<Method> accessor = ALL_ENTRY_ACCESSORS.computeIfAbsent(list.getClass(), KeyBindsFilterController::findAllEntriesAccessor);
        if (accessor.isEmpty()) {
            return fallbackEntries;
        }

        try {
            Object value = accessor.get().invoke(list);
            if (value instanceof List<?> entries) {
                List<KeyBindsList.Entry> sourceEntries = new ArrayList<>();
                Set<KeyBindsList.Entry> seenEntries = Collections.newSetFromMap(new IdentityHashMap<>());
                for (Object entry : entries) {
                    if (entry instanceof KeyBindsList.Entry keyBindsEntry && seenEntries.add(keyBindsEntry)) {
                        sourceEntries.add(keyBindsEntry);
                    }
                }
                return sourceEntries;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return fallbackEntries;
    }

    private static String searchQuery(KeyBindsScreen screen) {
        Optional<Field> field = SEARCH_FIELDS.computeIfAbsent(screen.getClass(), KeyBindsFilterController::findSearchField);
        if (field.isEmpty()) {
            return "";
        }

        try {
            Object searchBox = field.get().get(screen);
            if (searchBox == null) {
                return "";
            }

            Optional<Method> valueAccessor = SEARCH_VALUE_ACCESSORS.computeIfAbsent(searchBox.getClass(), KeyBindsFilterController::findSearchValueAccessor);
            if (valueAccessor.isEmpty()) {
                return "";
            }

            Object value = valueAccessor.get().invoke(searchBox);
            return value instanceof String query ? query : "";
        } catch (ReflectiveOperationException ignored) {
            return "";
        }
    }

    private static Optional<Field> findSearchField(Class<?> screenClass) {
        for (String fieldName : List.of("search", "searchBox")) {
            Field field = findField(screenClass, fieldName);
            if (field != null) {
                field.setAccessible(true);
                return Optional.of(field);
            }
        }
        return Optional.empty();
    }

    private static Optional<Method> findSearchValueAccessor(Class<?> searchBoxClass) {
        Method method = findNoArgMethod(searchBoxClass, "getValue");
        if (method != null && String.class.isAssignableFrom(method.getReturnType())) {
            method.setAccessible(true);
            return Optional.of(method);
        }
        return Optional.empty();
    }

    private static Optional<Method> findAllEntriesAccessor(Class<?> listClass) {
        Method method = findNoArgMethod(listClass, "getAllEntries");
        if (method != null && List.class.isAssignableFrom(method.getReturnType())) {
            method.setAccessible(true);
            return Optional.of(method);
        }
        return Optional.empty();
    }

    private static boolean replaceVisibleEntries(KeyBindsList list, List<KeyBindsList.Entry> newEntries) {
        Optional<EntryListMutators> mutators = ENTRY_LIST_MUTATORS.computeIfAbsent(list.getClass(), KeyBindsFilterController::findEntryListMutators);
        if (mutators.isEmpty()) {
            return false;
        }

        try {
            EntryListMutators entryListMutators = mutators.get();
            entryListMutators.clearEntries.invoke(list);
            for (KeyBindsList.Entry entry : newEntries) {
                entryListMutators.addEntryInternal.invoke(list, entry);
            }
            return true;
        } catch (ReflectiveOperationException ignored) {
            return false;
        }
    }

    private static Optional<EntryListMutators> findEntryListMutators(Class<?> listClass) {
        Method allEntries = findNoArgMethod(listClass, "getAllEntries");
        Method clearEntries = findNoArgMethod(listClass, "clearEntries");
        Method addEntryInternal = findEntryMethod(listClass, "addEntryInternal");
        if (allEntries == null || clearEntries == null || addEntryInternal == null) {
            return Optional.empty();
        }

        allEntries.setAccessible(true);
        clearEntries.setAccessible(true);
        addEntryInternal.setAccessible(true);
        return Optional.of(new EntryListMutators(clearEntries, addEntryInternal));
    }

    private static Method findEntryMethod(Class<?> listClass, String methodName) {
        Class<?> currentClass = listClass;
        while (currentClass != null) {
            for (Method method : currentClass.getDeclaredMethods()) {
                if (
                        method.getName().equals(methodName)
                                && method.getParameterCount() == 1
                                && method.getParameterTypes()[0].isAssignableFrom(KeyBindsList.Entry.class)
                ) {
                    return method;
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        return null;
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

    private static void resetAmecsModifiers(KeyBindsScreen screen) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft == null || minecraft.options == null) {
            return;
        }

        if (AmecsModifierResetter.resetAll(minecraft.options.keyMappings)) {
            KeyMapping.resetMapping();
        }
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
            fingerprint = 31L * fingerprint + keyMapping.getName().hashCode();
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
        private long keyStateFingerprint;
        private boolean filterDirty;
        private Button unboundButton;
        private Button conflictsButton;
        private Button resetButton;
        private boolean confirmingReset;
    }

    private static final class EntryListMutators {
        private final Method clearEntries;
        private final Method addEntryInternal;

        private EntryListMutators(Method clearEntries, Method addEntryInternal) {
            this.clearEntries = clearEntries;
            this.addEntryInternal = addEntryInternal;
        }
    }

    private static final class AmecsModifierResetter {
        private static final AmecsModifierResetter INSTANCE = new AmecsModifierResetter();

        private final Method resetBoundModifiers;

        private AmecsModifierResetter() {
            Method resetBoundModifiersMethod = null;
            try {
                Class<?> apiClass = Class.forName("de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi");
                resetBoundModifiersMethod = apiClass.getMethod("resetBoundModifiers", KeyMapping.class);
            } catch (ReflectiveOperationException ignored) {
            }
            this.resetBoundModifiers = resetBoundModifiersMethod;
        }

        private static boolean resetAll(KeyMapping[] keyMappings) {
            return INSTANCE.reset(keyMappings);
        }

        private boolean reset(KeyMapping[] keyMappings) {
            if (this.resetBoundModifiers == null) {
                return false;
            }

            boolean resetAny = false;
            for (KeyMapping keyMapping : keyMappings) {
                try {
                    this.resetBoundModifiers.invoke(null, keyMapping);
                    resetAny = true;
                } catch (ReflectiveOperationException ignored) {
                }
            }
            return resetAny;
        }
    }

}
