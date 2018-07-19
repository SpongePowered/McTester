package org.spongepowered.mctester.internal.mixin;

import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.mctester.internal.interfaces.IMixinPlayerChunkMap;

import java.util.List;
import java.util.Set;

@Mixin(PlayerChunkMap.class)
public abstract class MixinPlayerChunkMap implements IMixinPlayerChunkMap {

    @Shadow @Final private Set<PlayerChunkMapEntry> dirtyEntries;

    @Shadow @Final private List<PlayerChunkMapEntry> pendingSendToPlayers;

    @Shadow @Final private List<PlayerChunkMapEntry> entriesWithoutChunks;

    @Override
    public Set<PlayerChunkMapEntry> getDirtyEntries() {
        return this.dirtyEntries;
    }

    @Override
    public List<PlayerChunkMapEntry> getPendingSendToPlayers() {
        return this.pendingSendToPlayers;
    }

    @Override
    public List<PlayerChunkMapEntry> getEntriesWithoutChunks() {
        return this.entriesWithoutChunks;
    }
}
