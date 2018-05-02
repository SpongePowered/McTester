package org.spongepowered.mctester.main.framework;

import org.spongepowered.mctester.main.McTester;
import org.spongepowered.mctester.main.OneShotEventListener;
import org.spongepowered.mctester.main.TestUtils;
import org.spongepowered.mctester.main.appclass.ErrorSlot;
import org.spongepowered.mctester.main.framework.proxy.MainThreadProxy;
import org.spongepowered.mctester.main.framework.proxy.RemoteClientProxy;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.mctester.main.framework.proxy.ProxyCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

public class TesterManager implements /*Runnable,*/ TestUtils, ProxyCallback {

    private List<OneShotEventListener> listeners = new ArrayList<>();
    public Game fakeGame;
    public Client client;
    public ErrorSlot errorSlot = new ErrorSlot();

    /*public static void runTestThread() {
        TesterManager manager = new TesterManager();
        Thread thread = new Thread(manager);
        thread.setName("Tester-thread");
        thread.start();
    }*/

    public TesterManager() {
        Game realGame = Sponge.getGame();
        this.fakeGame = MainThreadProxy.newProxy(realGame, null);
        this.client = RemoteClientProxy.newProxy(this);
    }

    /*@Override
    public void run() {


        //ChatTest chatTest = new ChatTest();
        //chatTest.runTest(fakeGame, client, this);
    }*/

    @Override
    public <T extends Event> void listenOneShot(Class<T> eventClass, EventListener<? super T> listener) {
        OneShotEventListener oneShot = new OneShotEventListener(eventClass, listener, this.errorSlot);
        Sponge.getEventManager().registerListener(McTester.INSTANCE, eventClass, oneShot);
        this.listeners.add(oneShot);
    }

    @Override
    public void sleepTicks(int ticks) {
        FutureTask<?> task = new FutureTask<>((() -> {}), null);
        Sponge.getScheduler().createTaskBuilder().delayTicks(ticks).execute(task).submit(McTester.INSTANCE);
        try {
            task.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterInvoke() throws Throwable {
        for (OneShotEventListener listener: this.listeners) {
            if (!listener.handled) {
                McTester.INSTANCE.logger.error("One shot listener didn't run: " + listener.listener);
            }
            Sponge.getEventManager().unregisterListeners(listener);
        }
        listeners.clear();

        this.errorSlot.throwIfSet();
    }
}
