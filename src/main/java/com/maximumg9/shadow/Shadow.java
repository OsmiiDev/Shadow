package com.maximumg9.shadow;

import com.maximumg9.shadow.abilities.NetherStarItem;
import com.maximumg9.shadow.commands.DebugCommand;
import com.maximumg9.shadow.commands.LocationCommand;
import com.maximumg9.shadow.util.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.util.*;
import java.util.function.Function;

public class Shadow {

    public static final Map<Identifier, Function<Item.Settings,Item>> modifiedItems = new HashMap<>();

    static {
        modifiedItems.put(Identifier.ofVanilla("nether_star"), NetherStarItem::new);
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        DebugCommand.register(dispatcher);
        LocationCommand.register(dispatcher);
    }

    private final MinecraftServer server;

    public Config config = new Config();
    public BlockPos currentLocation = null;

    public GamePhase phase = GamePhase.NOT_PLAYING;

    public ChunkPos strongholdChunkPosition = null;
    public List<ChunkPos> playedStrongholdPositions = new ArrayList<>();
    private final HashMap<UUID,IndirectPlayer> indirectPlayers = new HashMap<>();

    public Shadow(MinecraftServer server) {
        this.server = server;
    }

    public IndirectPlayer getIndirect(ServerPlayerEntity base) {
        return indirectPlayers.computeIfAbsent(base.getUuid(),(uuid) -> new IndirectPlayer(base));
    }

    public MinecraftServer getServer() {return this.server;}
}
