package pw.aaron1011.mctester;

import org.spongepowered.api.event.EventListener;

public class OneShotEventListener<T> implements EventListener<T> {

    public Class<T> eventClass;
    public EventListener<? super T> listener;
    public boolean handled;

    public OneShotEventListener(Class<T> eventClass, EventListener<? super T> listener) {
        this.eventClass = eventClass;
        this.listener = listener;
    }

    @Override
    public void handle(T event) throws Exception {
        this.handled = true;
        this.listener.handle(event);
    }
}
