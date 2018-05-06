package org.spongepowered.mctester.junit;

import java.lang.annotation.Annotation;

public class DefaultMinecraftRunnerOptions implements MinecraftRunnerOptions {

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
        return MinecraftRunnerOptions.class;
    }
}
