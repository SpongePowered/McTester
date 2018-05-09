package org.spongepowered.mctester.internal;

import com.google.inject.Inject;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.SpongeExecutorService;
import org.spongepowered.api.scheduler.SynchronousExecutor;

@Plugin(id = "mctester_dummy", name = "McTester Dummmy", description = "McTester's dummy event handler plugin", version = "1.0-SNAPSHOT")
public class McTesterDummy {

    public static McTesterDummy INSTANCE;

    @Inject
    @SynchronousExecutor
    public SpongeExecutorService syncExecutor;

    public McTesterDummy() {
        INSTANCE = this;
    }

}
