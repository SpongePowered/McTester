package org.spongepowered.mctester.api;

import java.lang.annotation.Annotation;

public class DefaultWorldOptions implements WorldOptions {

    @Override
    public boolean deleteWorldOnSuccess() {
        return true;
    }

    @Override
    public boolean deleteWorldOnFailure() {
        return false;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return WorldOptions.class;
    }
}
