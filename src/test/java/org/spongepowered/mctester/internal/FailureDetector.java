package org.spongepowered.mctester.internal;

import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

import java.util.Optional;

public class FailureDetector extends RunListener {

    public Optional<Boolean> succeeded;

    @Override
    public void testRunFinished(Result result) throws Exception {
        this.succeeded = Optional.of(result.wasSuccessful());
    }

}
