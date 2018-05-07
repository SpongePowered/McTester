package org.spongepowered.mctester.junit;

import java.lang.annotation.Annotation;

public class DefaultScreenshotOptions implements ScreenshotOptions {

    @Override
    public boolean takeScreenshotOnSuccess() {
        return false;
    }

    @Override
    public boolean takeScreenshotOnFailure() {
        return true;
    }

    @Override
    public int delayTicks() {
        return 20;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ScreenshotOptions.class;
    }
}
