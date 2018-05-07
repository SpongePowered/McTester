package org.spongepowered.mctester.internal;

import com.google.common.util.concurrent.Futures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ScreenShotHelper;
import org.junit.runners.model.FrameworkMethod;
import org.spongepowered.mctester.internal.world.CurrentWorld;
import org.spongepowered.mctester.junit.ScreenshotOptions;
import org.spongepowered.mctester.junit.UseSeparateWorld;
import org.spongepowered.mctester.junit.WorldOptions;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

public class TestActions {

    private final TestUtils testUtils;
    private final WorldOptions globalWorldOptions;
    private final ScreenshotOptions globalScreenshotOptions;
    private final TestStatus globalStatus;

    public TestActions(TestUtils testUtils, WorldOptions globalWorldOptions, ScreenshotOptions globalScreenshotOptions, TestStatus globalStatus) {
        this.testUtils = testUtils;
        this.globalWorldOptions = globalWorldOptions;
        this.globalScreenshotOptions = globalScreenshotOptions;
        this.globalStatus = globalStatus;
    }


    public boolean shouldDeleteWorldGlobal() {
        return (this.globalWorldOptions.deleteWorldOnSuccess() && this.globalStatus.succeeded())
            || (this.globalWorldOptions.deleteWorldOnFailure() && this.globalStatus.failed());
    }

    public boolean shouldDeleteTempWorld(FrameworkMethod method, TestStatus status) {
        WorldOptions options = method.getAnnotation(UseSeparateWorld.class).worldOptions();
        return (status.succeeded() && options.deleteWorldOnSuccess()) || (status.failed() && options.deleteWorldOnFailure());
    }

    public void tryDeleteWorldGlobal(CurrentWorld world) {
        if (this.shouldDeleteWorldGlobal()) {
            world.deleteWorld();
        }
    }

    public void tryDeleteTempWorld(FrameworkMethod method, TestStatus status, CurrentWorld world) {
        if (this.shouldDeleteTempWorld(method, status)) {
            world.deleteWorld();
        }
    }


    public Optional<Integer> shouldTakeScreenshotGlobal() {
        if ((this.globalScreenshotOptions.takeScreenshotOnSuccess() && this.globalStatus.succeeded())
            || (this.globalScreenshotOptions.takeScreenshotOnFailure() && this.globalStatus.failed())) {
            return Optional.of(this.globalScreenshotOptions.delayTicks());
        }
        return Optional.empty();
    }

    public Optional<Integer> shouldTakeScreenshotCustomOptions(FrameworkMethod method, TestStatus status) {
        ScreenshotOptions options = method.getAnnotation(ScreenshotOptions.class);
        if (options != null && ((status.succeeded() && options.takeScreenshotOnSuccess()) || (status.failed() && options.takeScreenshotOnFailure()))) {
            return Optional.of(options.delayTicks());
        }
        return Optional.empty();
    }

    public void tryTakeScreenShotGlobal(String name) {
        this.shouldTakeScreenshotGlobal().ifPresent(ticks -> this.takeScreenshot(name, ticks));
    }

    public void tryTakeScreenShotCustom(FrameworkMethod method, TestStatus status) {
        this.shouldTakeScreenshotCustomOptions(method, status).ifPresent(ticks -> this.takeScreenshot(method.getName() + "-", ticks));
    }


    private File takeScreenshot(String baseName, int ticks) {
        this.testUtils.sleepTicks(ticks);

        return Futures.getUnchecked(Minecraft.getMinecraft().addScheduledTask(new Callable<File>() {

            @Override
            public File call() {
                File outputDir = new File(Minecraft.getMinecraft().mcDataDir, "screenshots");

                File outputFile = Utils.getTimestampedPNGFileForDirectory(baseName, outputDir);
                BufferedImage screenshot = ScreenShotHelper
                        .createScreenshot(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, Minecraft.getMinecraft().getFramebuffer());

                try {
                    ImageIO.write(screenshot, "png", outputFile);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return outputFile;
            }
        }));
    }

}
