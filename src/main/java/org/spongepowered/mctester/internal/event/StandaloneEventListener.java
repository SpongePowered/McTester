package org.spongepowered.mctester.internal.event;

import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;

public interface StandaloneEventListener<T extends Event> extends EventListener<T> {

    Class<T> getEventClass();

}
