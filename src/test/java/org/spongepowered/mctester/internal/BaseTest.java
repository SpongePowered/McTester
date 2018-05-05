package org.spongepowered.mctester.internal;

import org.spongepowered.api.Game;
import org.spongepowered.mctester.internal.TestUtils;
import org.spongepowered.mctester.internal.framework.Client;

public abstract class BaseTest {

    protected Game game;
    protected Client client;
    protected TestUtils testUtils;

    public BaseTest(TestUtils testUtils) {
        this.testUtils = testUtils;
        this.game = testUtils.getGame();
        this.client = testUtils.getClient();

    }

}
