package org.spongepowered.mctester.internal;

import org.spongepowered.api.Game;
import org.spongepowered.mctester.junit.Client;
import org.spongepowered.mctester.junit.TestUtils;

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
