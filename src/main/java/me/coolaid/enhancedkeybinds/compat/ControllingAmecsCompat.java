package me.coolaid.enhancedkeybinds.compat;

import com.mojang.blaze3d.platform.InputConstants;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiers;
import de.siphalor.amecs.key_modifiers.api.AmecsKeyModifiersApi;
import de.siphalor.amecs.key_modifiers.impl.KeyBindingEditGuiHelper;
import me.coolaid.enhancedkeybinds.EnhancedKeybinds;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public final class ControllingAmecsCompat {
    private ControllingAmecsCompat() {
    }

    public static void init() {
        if (!FabricLoader.getInstance().isModLoaded("controlling")) {
            return;
        }

        try {
            Class<?> eventsClass = Class.forName("com.blamejared.controlling.api.event.ControllingEvents");
            Class<?> handlerClass = Class.forName("com.blamejared.controlling.api.event.IEventHandler");

            register(eventsClass, handlerClass, "IS_KEY_CODE_MODIFIER_EVENT", event -> {
                InputConstants.Key key = eventValue(event, InputConstants.Key.class, "key");
                return AmecsKeyModifiers.fromKey(key) != null;
            });
            register(eventsClass, handlerClass, "KEY_ENTRY_MOUSE_CLICKED_EVENT", event -> {
                Object entry = eventValue(event, Object.class, "entry", "getEntry");
                MouseButtonEvent mouseEvent = eventValue(event, MouseButtonEvent.class, "event");
                Button changeButton = entryValue(entry, "getBtnChangeKeyBinding", Button.class);
                if (isMouseOver(changeButton, mouseEvent)) {
                    entryValue(entry, "getKey", KeyMapping.class).setKey(InputConstants.UNKNOWN);
                }
                return false;
            });
            register(eventsClass, handlerClass, "SET_TO_DEFAULT_EVENT", event -> {
                KeyMapping mapping = eventValue(event, KeyMapping.class, "mapping");
                AmecsKeyModifiersApi.resetBoundModifiers(mapping);
                return false;
            });
            register(eventsClass, handlerClass, "SET_KEY_EVENT", event -> {
                KeyMapping mapping = eventValue(event, KeyMapping.class, "mapping");
                InputConstants.Key key = eventValue(event, InputConstants.Key.class, "key");
                KeyBindingEditGuiHelper.handleKeyPress(mapping, key);
                return true;
            });
        } catch (ReflectiveOperationException | RuntimeException exception) {
            EnhancedKeybinds.LOGGER.warn("Failed to register Controlling Amecs compatibility", exception);
        }
    }

    @SuppressWarnings("unchecked")
    private static void register(Class<?> eventsClass, Class<?> handlerClass, String eventFieldName, EventResponder responder)
            throws ReflectiveOperationException {
        Field eventField = eventsClass.getField(eventFieldName);
        Event<Object> event = (Event<Object>) eventField.get(null);
        Object listener = Proxy.newProxyInstance(
                handlerClass.getClassLoader(),
                new Class<?>[]{handlerClass},
                (proxy, method, args) -> {
                    if (method.getDeclaringClass() == Object.class) {
                        return method.invoke(ControllingAmecsCompat.class, args);
                    }
                    if ("handle".equals(method.getName()) && args != null && args.length == 1) {
                        try {
                            return responder.handle(args[0]);
                        } catch (ReflectiveOperationException | RuntimeException exception) {
                            EnhancedKeybinds.LOGGER.warn("Failed to handle Controlling Amecs event {}", eventFieldName, exception);
                            return false;
                        }
                    }
                    throw new UnsupportedOperationException(method.toString());
                }
        );
        event.register(listener);
    }

    private static <T> T eventValue(Object event, Class<T> type, String... methodNames) throws ReflectiveOperationException {
        NoSuchMethodException missingMethod = null;
        for (String methodName : methodNames) {
            try {
                Method method = event.getClass().getMethod(methodName);
                Object value = method.invoke(event);
                return type.cast(value);
            } catch (NoSuchMethodException exception) {
                if (missingMethod == null) {
                    missingMethod = exception;
                } else {
                    missingMethod.addSuppressed(exception);
                }
            }
        }

        if (missingMethod != null) {
            throw missingMethod;
        }
        throw new NoSuchMethodException("No Controlling event accessor names were provided");
    }

    private static <T> T entryValue(Object entry, String methodName, Class<T> type) throws ReflectiveOperationException {
        Method method = entry.getClass().getMethod(methodName);
        Object value = method.invoke(entry);
        return type.cast(value);
    }

    private static boolean isMouseOver(Button button, MouseButtonEvent event) {
        return event.x() >= button.getX()
                && event.y() >= button.getY()
                && event.x() < button.getX() + button.getWidth()
                && event.y() < button.getY() + button.getHeight();
    }

    @FunctionalInterface
    private interface EventResponder {
        Object handle(Object event) throws ReflectiveOperationException;
    }
}
