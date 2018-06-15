package org.spongepowered.mctester.internal.event;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;

public class StandaloneEventListener<T extends Event> implements EventListener<T> {

    private final Class<T> eventClass;
    protected final EventListener<T> listener;

    public StandaloneEventListener(Class<T> eventClass, EventListener<T> listener) {
        this.eventClass = eventClass;
        this.listener = listener;
    }

    public Class<T> getEventClass() {
        return this.eventClass;
    }

    @Override
    public void handle(T event) throws Exception {
        this.listener.handle(event);
    }
}
