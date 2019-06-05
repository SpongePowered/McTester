package org.spongepowered.mctester.internal.event;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.api.event.Order;

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

    public boolean beforeModifications() {
        return false;
    }

    public Order order() {
        return Order.DEFAULT;
    }

    @Override
    public void handle(T event) throws Exception {
        this.listener.handle(event);
    }
}
