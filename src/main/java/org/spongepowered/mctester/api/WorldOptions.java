package org.spongepowered.mctester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controls world-specific options
 *
 * This can be applied
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface WorldOptions {

    /**
     * Whether or not to delete the world if all associated tests succeeded
     */
    boolean deleteWorldOnSuccess() default true;

    /**
     * Where or not to delete the world if at least one associated test failed
     */
    boolean deleteWorldOnFailure() default false;

}
