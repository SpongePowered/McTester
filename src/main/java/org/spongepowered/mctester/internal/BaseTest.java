package org.spongepowered.mctester.internal;

import org.spongepowered.api.Game;
import org.spongepowered.mctester.junit.Client;
import org.spongepowered.mctester.junit.TestUtils;

public abstract class BaseTest {

    protected Client client;
    protected TestUtils testUtils;

    public BaseTest(TestUtils testUtils) {
        this.testUtils = testUtils;
        this.client = testUtils.getClient();

    }

}
