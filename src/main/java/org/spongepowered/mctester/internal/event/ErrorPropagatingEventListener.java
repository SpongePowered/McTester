package org.spongepowered.mctester.internal.event;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.mctester.internal.appclass.ErrorSlot;

public class ErrorPropagatingEventListener<T extends Event> implements StandaloneEventListener<T> {

    private Class<T> eventClass;
    EventListener<? super T> listener;
    ErrorSlot errorSlot;

    public ErrorPropagatingEventListener(Class<T> eventClass, EventListener<? super T> listener, ErrorSlot errorSlot) {
        this.eventClass = eventClass;
        this.listener = listener;
        this.errorSlot = errorSlot;
    }

    @Override
    public void handle(T event) throws Exception {
        try {
            this.listener.handle(event);
        } catch (Throwable e) {
            this.errorSlot.setErrorIfUnset(e);
            throw e;
        }
    }

    @Override
    public Class<T> getEventClass() {
        return this.eventClass;
    }

}
