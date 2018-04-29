package pw.aaron1011.mctester;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import pw.aaron1011.mctester.framework.TesterHandler;

public interface TestUtils {

    <T extends Event> void listenOneShot(Class<T> eventClass, EventListener<? super T> listener);
}
