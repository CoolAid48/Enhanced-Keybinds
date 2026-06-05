package me.coolaid.enhancedkeybinds.screen;

import net.minecraft.client.gui.screens.options.controls.KeyBindsList;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class KeyBindsListEntryRefresher {
    private static final Map<Class<?>, Optional<Method>> REFRESH_METHODS = new HashMap<>();

    private KeyBindsListEntryRefresher() {
    }

    public static void refresh(KeyBindsList.Entry entry) {
        Optional<Method> method = REFRESH_METHODS.computeIfAbsent(entry.getClass(), KeyBindsListEntryRefresher::findRefreshMethod);
        if (method.isEmpty()) {
            return;
        }

        try {
            method.get().invoke(entry);
        } catch (IllegalAccessException ignored) {
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof AbstractMethodError) {
                return;
            }
            if (cause instanceof RuntimeException runtimeException) {
                throw runtimeException;
            }
            if (cause instanceof Error error) {
                throw error;
            }
        }
    }

    private static Optional<Method> findRefreshMethod(Class<?> entryClass) {
        Class<?> currentClass = entryClass;
        while (currentClass != null) {
            try {
                Method method = currentClass.getDeclaredMethod("refreshEntry");
                if (
                        method.getParameterCount() == 0
                                && method.getReturnType() == void.class
                                && !Modifier.isAbstract(method.getModifiers())
                ) {
                    method.setAccessible(true);
                    return Optional.of(method);
                }
            } catch (NoSuchMethodException ignored) {
            }
            currentClass = currentClass.getSuperclass();
        }
        return Optional.empty();
    }
}
