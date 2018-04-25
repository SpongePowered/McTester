package pw.aaron1011.mctester;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import pw.aaron1011.mctester.framework.TesterHandler;

public class TestUtils {

    public static <T extends Event> void listenOneShot(Class<T> eventClass, EventListener<? super T> listener) {
        OneShotEventListener oneShot = new OneShotEventListener(eventClass, listener);
        Sponge.getEventManager().registerListener(McTester.INSTANCE, eventClass, oneShot);
        McTester.INSTANCE.handler.addOneShot(oneShot);
    }

    public static void waitForSignal() {
        try {
            TesterHandler.lock.lock();
            TesterHandler.condition.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            TesterHandler.lock.unlock();
        }
    }

}
