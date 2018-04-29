package pw.aaron1011.mctester.framework;

import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import pw.aaron1011.mctester.McTester;
import pw.aaron1011.mctester.OneShotEventListener;
import pw.aaron1011.mctester.TestUtils;
import pw.aaron1011.mctester.framework.proxy.MainThreadProxy;
import pw.aaron1011.mctester.framework.proxy.ProxyCallback;
import pw.aaron1011.mctester.framework.proxy.RemoteClientProxy;
import pw.aaron1011.mctester.testcase.ChatTest;

import java.util.ArrayList;
import java.util.List;

public class TesterThread extends Thread implements TestUtils, ProxyCallback {

    public TesterThread() {
        this.setName("Tester Thread");
    }

    private List<OneShotEventListener> listeners = new ArrayList<>();

    public static void runTests() {
        TesterThread thread = new TesterThread();
        thread.start();
    }

    @Override
    public void run() {
        Game realGame = Sponge.getGame();
        Game fakeGame = MainThreadProxy.newProxy(realGame);
        Client client = RemoteClientProxy.newProxy();

        ChatTest chatTest = new ChatTest();
        chatTest.runTest(fakeGame, client, this);
    }

    @Override
    public <T extends Event> void listenOneShot(Class<T> eventClass, EventListener<? super T> listener) {
        OneShotEventListener oneShot = new OneShotEventListener(eventClass, listener);
        Sponge.getEventManager().registerListener(McTester.INSTANCE, eventClass, oneShot);
        this.listeners.add(oneShot);
    }

    @Override
    public void afterInvoke() {
        for (OneShotEventListener listener: this.listeners) {
            if (!listener.handled) {
                McTester.INSTANCE.logger.error("One shot listener didn't run: " + listener.listener);
            }
            Sponge.getEventManager().unregisterListeners(listener);
        }
        listeners.clear();
    }
}
