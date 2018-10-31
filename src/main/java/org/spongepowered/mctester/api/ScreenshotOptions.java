package org.spongepowered.mctester.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Inherited
public @interface ScreenshotOptions {

    /**
     * Whether or not to take a screenshot of the world if all associated tests succeeded
     * @return The value
     */
    boolean takeScreenshotOnSuccess() default false;

    /**
     * Where or not to take a screenshot of the world if at least one associated test failed
     * @return The value
     */
    boolean takeScreenshotOnFailure() default true;

    /**
     * The number of ticks to delay before taking a screenshot
     *
     * <p>Depending on the operations your test plugins was performing,
     * your changes to the world might not have fully rendered by the time
     * thew screenshot is taken. If your screenshot doesn't look right,
     * try increasing this value.</p>
     * @return The number of ticks to delay
     */
    int delayTicks() default 20;

}
