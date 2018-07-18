package org.spongepowered.mctester.internal.event;

import org.spongepowered.api.event.Event;
import org.spongepowered.mctester.internal.appclass.ErrorSlot;

public class ErrorPropagatingEventListener<T extends Event> extends StandaloneEventListener<T> {

    ErrorSlot errorSlot;

    public ErrorPropagatingEventListener(StandaloneEventListener<T> listener, ErrorSlot errorSlot) {
        super(listener.getEventClass(), listener);
        this.errorSlot = errorSlot;
    }

    /*
    private static <V extends Event> Class<V> getClassFromListener(EventListener<V> listener) {
        List<Method> handleMethods = Arrays.stream(listener.getClass().getMethods()).filter(m -> m.getName().equals("handle") && m.getParameterTypes().length > 0 && m.getParameterTypes()[0] != Event.class).collect(Collectors.toList());
        if (handleMethods.size() != 1) {
            // This should be impossible
            throw new IllegalStateException(String.format("Event listener %s had unexpected'handle' methods: %s", listener, handleMethods));
        }
        Class<V> eventClass = (Class) handleMethods.get(0).getParameterTypes()[0];
        return eventClass;
    }*/

    @Override
    public void handle(T event) throws Exception {
        try {
            this.listener.handle(event);
        } catch (Throwable e) {
            this.errorSlot.setErrorIfUnset(e);
            throw e;
        }
    }
}
