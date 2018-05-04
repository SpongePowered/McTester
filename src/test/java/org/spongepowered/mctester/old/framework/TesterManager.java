package org.spongepowered.mctester.old.framework;

import org.spongepowered.mctester.old.appclass.ErrorSlot;
import org.spongepowered.mctester.old.McTester;
import org.spongepowered.mctester.old.OneShotEventListener;
import org.spongepowered.mctester.old.TestUtils;
import org.spongepowered.mctester.old.framework.proxy.MainThreadProxy;
import org.spongepowered.mctester.old.framework.proxy.RemoteClientProxy;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.mctester.old.framework.proxy.ProxyCallback;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

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
    public <T> T batchActions(Callable<T> callable) throws Throwable {
        try {
            return McTester.INSTANCE.syncExecutor.schedule(callable, 0, TimeUnit.SECONDS).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Override
    public void batchActions(Runnable runnable) throws Throwable {
        this.batchActions(() -> {
            runnable.run();
            return null;
        });
    }

    @Override
    public void sleepTicks(int ticks) {
        if (Sponge.getServer().isMainThread()) {
            throw new IllegalStateException("Can't call TestUtils.sleepTicks from the main thread!");
        }
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
