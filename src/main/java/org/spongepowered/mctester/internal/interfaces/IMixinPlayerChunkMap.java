package org.spongepowered.mctester.internal.interfaces;

import net.minecraft.server.management.PlayerChunkMapEntry;

import java.util.List;
import java.util.Set;

public interface IMixinPlayerChunkMap {

    Set<PlayerChunkMapEntry> getDirtyEntries();

    List<PlayerChunkMapEntry> getPendingSendToPlayers();

    List<PlayerChunkMapEntry> getEntriesWithoutChunks();

}
