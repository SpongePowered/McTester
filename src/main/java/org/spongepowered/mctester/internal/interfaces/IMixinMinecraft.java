package org.spongepowered.mctester.internal.interfaces;

import com.google.common.util.concurrent.ListenableFuture;

import java.util.concurrent.Callable;

public interface IMixinMinecraft {

    boolean isRunning();

    void leftClick();

    void holdLeftClick(boolean clicking);

    void rightClick();

    void holdRightClick(boolean clicking);

    <T> ListenableFuture<T> addScheduledTaskAlwaysDelay(Callable<T> callable);

}
