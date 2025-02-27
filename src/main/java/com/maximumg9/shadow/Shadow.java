package com.maximumg9.shadow;

import com.maximumg9.shadow.abilities.NetherStarItem;
import com.maximumg9.shadow.commands.DebugCommand;
import com.maximumg9.shadow.commands.EnderEyeItem;
import com.maximumg9.shadow.commands.LocationCommand;
import com.maximumg9.shadow.commands.StartCommand;
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

import java.io.File;
import java.util.*;
import java.util.function.Function;

public class Shadow implements Tickable {

    public static final Map<Identifier, Function<Item.Settings,Item>> modifiedItems = new HashMap<>();

    static {
        modifiedItems.put(Identifier.ofVanilla("nether_star"), NetherStarItem::new);
        modifiedItems.put(Identifier.ofVanilla("ender_eye"), EnderEyeItem::new);
    }

    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess commandRegistryAccess) {
        DebugCommand.register(dispatcher);
        LocationCommand.register(dispatcher);
        StartCommand.register(dispatcher);
    }

    private transient final MinecraftServer server;

    public transient Config config = new Config();
    public BlockPos currentLocation = null;

    public GamePhase phase = GamePhase.NOT_PLAYING;

    private transient final List<Tickable> tickables = new ArrayList<>();

    public ChunkPos strongholdChunkPosition = null;
    public List<ChunkPos> playedStrongholdPositions = new ArrayList<>();
    private transient final HashMap<UUID,IndirectPlayer> indirectPlayers = new HashMap<>();

    public Shadow(MinecraftServer server) {
        this.server = server;
    }

    public IndirectPlayer getIndirect(ServerPlayerEntity base) {
        return indirectPlayers.computeIfAbsent(base.getUuid(),(uuid) -> new IndirectPlayer(base));
    }

    public List<IndirectPlayer> getOnlinePlayers() {
        return this.server.getPlayerManager().getPlayerList().stream().map(this::getIndirect).toList();
    }

    public MinecraftServer getServer() {return this.server;}

    public void addTickable(Tickable tickable) {
        this.tickables.add(tickable);
    }

    @Override
    public void tick() {
        List<Tickable> tickableCopy = this.tickables.stream().toList();
        for(Tickable tickable : tickableCopy) {
            tickable.tick();
            if(tickable.shouldEnd()) {
                tickable.onEnd();
                this.tickables.remove(tickable);
            }
        }
    }

    private transient File DATA_FILE = new File("shadow-data.json");

    public void save() {

    }

    public void load() {
    }

    public void removeTickables(Class<? extends Tickable> tickableClass) {
        this.tickables.removeIf((tickable -> tickable.getClass() == tickableClass));
    }
}
