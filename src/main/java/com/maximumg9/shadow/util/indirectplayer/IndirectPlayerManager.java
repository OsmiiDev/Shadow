package com.maximumg9.shadow.util.indirectplayer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.nbt.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class IndirectPlayerManager {
    private final Path saveFile;

    private final HashMap<UUID,IndirectPlayer> indirectPlayers = new HashMap<>();

    private final MinecraftServer server;

    public static final Gson GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        builder.excludeFieldsWithoutExposeAnnotation();
        GSON = builder.create();
    }

    public IndirectPlayerManager(IndirectPlayerManager other) {
        this.saveFile = other.saveFile;
        this.server = other.server;
        other.indirectPlayers.forEach(
                (uuid,indirectPlayer) -> this.indirectPlayers.put(uuid, new IndirectPlayer(indirectPlayer))
        );
    }

    public IndirectPlayerManager(File saveFile, MinecraftServer server) {
        this.saveFile = saveFile.toPath();
        this.server = server;
    }

    public void load() throws IOException {
        NbtIo.readCompressed(this.saveFile, new NbtSizeTracker(0xffffffffffffL,256));
    }

    private void readNbt(NbtCompound nbt) {
        NbtList list = nbt.getList("indirectPlayers", NbtElement.COMPOUND_TYPE);

        this.indirectPlayers.clear();

        for(int i=0;i<list.size();i++) {
            NbtCompound indirectPlayerData = list.getCompound(i);
            IndirectPlayer player = IndirectPlayer.load(this.server, indirectPlayerData);
            this.indirectPlayers.put(player.playerUUID, player);
        }
    }

    public void save() throws IOException {
        NbtCompound data = this.writeNbt(new NbtCompound());
        NbtIo.writeCompressed(data,this.saveFile);
    }

    private NbtCompound writeNbt(NbtCompound nbt) {
        NbtList lv = new NbtList();
        for (IndirectPlayer player : indirectPlayers.values()) {
            lv.add(player.save(new NbtCompound()));
        }

        nbt.put("indirectPlayers", lv);

        return nbt;
    }

    public Collection<IndirectPlayer> getAllPlayers() {
        return this.indirectPlayers.values();
    }

    public IndirectPlayer getIndirect(ServerPlayerEntity base) {
        return this.indirectPlayers.computeIfAbsent(base.getUuid(),(uuid) -> new IndirectPlayer(base));
    }

}
