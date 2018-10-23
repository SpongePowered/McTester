package org.spongepowered.mctester.internal;

import com.google.common.util.concurrent.Futures;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ScreenShotHelper;
import org.junit.runners.model.FrameworkMethod;
import org.spongepowered.mctester.api.ScreenshotOptions;
import org.spongepowered.mctester.api.UseSeparateWorld;
import org.spongepowered.mctester.api.WorldOptions;
import org.spongepowered.mctester.api.junit.TestStatus;
import org.spongepowered.mctester.internal.world.CurrentWorld;
import org.spongepowered.mctester.junit.TestUtils;

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

    private static class ScreenshotData {
        int ticks;
        boolean success;

        ScreenshotData(int ticks, boolean success) {
            this.ticks = ticks;
            this.success =success;
        }

        String getDirName() {
            if (this.success) {
                return "success";
            }
            return "fail";
        }
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


    public Optional<ScreenshotData> shouldTakeScreenshotGlobal() {
        boolean success = this.globalScreenshotOptions.takeScreenshotOnSuccess() && this.globalStatus.succeeded();
        boolean fail =  this.globalScreenshotOptions.takeScreenshotOnFailure() && this.globalStatus.failed();
        if (success || fail) {
            return Optional.of(new ScreenshotData(this.globalScreenshotOptions.delayTicks(), success));
        }
        return Optional.empty();
    }

    public Optional<ScreenshotData> shouldTakeScreenshotCustomOptions(FrameworkMethod method, TestStatus status) {
        ScreenshotOptions options = method.getAnnotation(ScreenshotOptions.class);
        if (options == null) {
            return Optional.empty();
        }

        boolean success = status.succeeded() && options.takeScreenshotOnSuccess();
        boolean fail = status.failed() && options.takeScreenshotOnFailure();

        if (success || fail) {
            return Optional.of(new ScreenshotData(options.delayTicks(), success));
        }
        return Optional.empty();
    }

    public void tryTakeScreenShotGlobal(String name) {
        this.shouldTakeScreenshotGlobal().ifPresent(data -> this.takeScreenshot(name, data));
    }

    public void tryTakeScreenShotCustom(FrameworkMethod method, TestStatus status) {
        this.shouldTakeScreenshotCustomOptions(method, status).ifPresent(data -> this.takeScreenshot(method.getName() + "-", data));
    }


    private File takeScreenshot(String baseName, ScreenshotData data) {
        this.testUtils.sleepTicks(data.ticks);

        return Futures.getUnchecked(Minecraft.getMinecraft().addScheduledTask(() -> {
            File outputDir = new File(Minecraft.getMinecraft().gameDir, "screenshots");
            outputDir = new File(outputDir, data.getDirName());
            outputDir.mkdirs();

            File outputFile = Utils.getTimestampedPNGFileForDirectory(baseName, outputDir);
            BufferedImage screenshot = ScreenShotHelper
                    .createScreenshot(Minecraft.getMinecraft().displayWidth, Minecraft.getMinecraft().displayHeight, Minecraft.getMinecraft().getFramebuffer());

            try {
                ImageIO.write(screenshot, "png", outputFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return outputFile;
        }));
    }

}
