package org.spongepowered.mctester.internal.framework;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.mctester.internal.appclass.ErrorSlot;
import org.spongepowered.mctester.internal.McTester;
import org.spongepowered.mctester.internal.OneShotEventListener;
import org.spongepowered.mctester.internal.TestUtils;
import org.spongepowered.mctester.internal.framework.proxy.MainThreadProxy;
import org.spongepowered.mctester.internal.framework.proxy.RemoteClientProxy;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.EventListener;
import org.spongepowered.mctester.internal.framework.proxy.ProxyCallback;

import java.util.ArrayList;
import java.util.Collection;
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
        this.client = new ServerSideClientHandler(RemoteClientProxy.newProxy(this));
    }

    /*@Override
    public void run() {


        //ChatTest chatTest = new ChatTest();
        //chatTest.runTest(fakeGame, client, this);
    }*/

    @Override
    public Game getGame() {
        return this.fakeGame;
    }

    @Override
    public Client getClient() {
        return this.client;
    }

    @Override
    public Player getThePlayer() {
        Collection<Player> players = fakeGame.getServer().getOnlinePlayers();
        if (players.size() != 1) {
            throw new RuntimeException("Unexpected players: " + players);
        }
        return players.iterator().next();
    }

    @Override
    public <T extends Event> EventListener<T> listenOneShot(Class<T> eventClass, EventListener<? super T> listener) {
        AssertionError error = new AssertionError("The one shot event listener registered here failed to run in time!\n");
        error.fillInStackTrace();

        OneShotEventListener oneShot = new OneShotEventListener(eventClass, listener, this.errorSlot, error);
        Sponge.getEventManager().registerListener(McTester.INSTANCE, eventClass, oneShot);
        this.listeners.add(oneShot);

        return oneShot;
    }

    @Override
    public <T> T batchActions(Callable<T> callable) throws Throwable {
        if (Sponge.getServer().isMainThread()) {
            throw new IllegalStateException("Calling batchActions from the main thread is pointless!");
        }

        try {
            return McTester.INSTANCE.syncExecutor.schedule(callable, 0, TimeUnit.SECONDS).get();
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Override
    public void batchActions(Runnable runnable) throws Throwable {
        if (Sponge.getServer().isMainThread()) {
            throw new IllegalStateException("Calling batchActions from the main thread is pointless!");
        }

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
        // This runs after a Client method has returned
        for (OneShotEventListener listener: this.listeners) {
            if (!listener.handled) {
                throw new AssertionError("A one-shot listener failed to run in time!\n" +
                                         "By the time this method call finished, a one-shot event listener should have been run - but it wasn't.\n"  +
                                         "See the nested stacktrace for the location of the one-shot event handler.", listener.fakeError);
            }
            Sponge.getEventManager().unregisterListeners(listener);
        }
        listeners.clear();

        this.errorSlot.throwIfSet();
    }

    @Override
    public void waitForInventoryPropagation() {
        this.sleepTicks(2);
    }
}
