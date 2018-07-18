package org.spongepowered.mctester.internal.interfaces;

import javax.annotation.Nullable;

public interface IMixinMinecraftServer {

    @Nullable Thread getServerMainThread();

}
