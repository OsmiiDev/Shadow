package com.maximumg9.shadow;

import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class GameState implements Cloneable {
    public GamePhase phase = GamePhase.NOT_PLAYING;

    public BlockPos currentLocation = null;
    public ChunkPos strongholdChunkPosition = null;
    public List<ChunkPos> playedStrongholdPositions = new ArrayList<>();
    public List<Eye> eyes = new ArrayList<>();

    public GameState() {}

    // Allow some values to persist from last state, I hate the compiler complaining here it doesn't even make sense
    @SuppressWarnings("CopyConstructorMissesField")
    public GameState(GameState lastState) {
        this.playedStrongholdPositions = lastState.playedStrongholdPositions;
    }

    @SuppressWarnings("unchecked")
    @Override
    public GameState clone() {
        try {
            GameState clone = (GameState) super.clone();
            clone.phase = this.phase;
            clone.currentLocation = this.currentLocation;
            clone.strongholdChunkPosition = this.strongholdChunkPosition;
            clone.playedStrongholdPositions = this.playedStrongholdPositions;
            clone.eyes = this.eyes;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
