package org.spongepowered.mctester.api.junit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.util.Optional;

public class TestStatus extends RunListener {

    private boolean succeeded = true;


    @Override
    public void testFailure(Failure failure) throws Exception {
        this.succeeded = false;
    }

    @Override
    public void testFinished(Description description) throws Exception {
    }

    /*@Override
    public void testRunFinished(Result result) throws Exception {
        this.succeeded = Optional.of(result.wasSuccessful());
        this.statusCallback.onFinished();
    }*/

    public boolean succeeded() {
        return this.succeeded;
    }

    public boolean failed() {
        return !this.succeeded;
    }


}
