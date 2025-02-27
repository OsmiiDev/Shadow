package com.maximumg9.shadow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonWriter;
import com.maximumg9.shadow.abilities.NetherStarItem;
import com.maximumg9.shadow.commands.DebugCommand;
import com.maximumg9.shadow.commands.EnderEyeItem;
import com.maximumg9.shadow.commands.LocationCommand;
import com.maximumg9.shadow.commands.StartCommand;
import com.maximumg9.shadow.util.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
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

    private final MinecraftServer server;
    public Config config = new Config();
    private final List<Tickable> tickables = new ArrayList<>();
    private final HashMap<UUID,IndirectPlayer> indirectPlayers = new HashMap<>();

    public GameState state = new GameState();

    public Shadow(MinecraftServer server) {
        this.server = server;

        try {
            this.loadSync();
        } catch (FileNotFoundException e) {
            LogUtils.getLogger().warn("Failed to load data, creating file");
            this.state = new GameState();
        } catch (IOException e) {
            LogUtils.getLogger().warn("Exception while loading data");
        }

        try {
            this.saveSync();
        } catch(IOException e) {
            LogUtils.getLogger().warn("Failed to save data");
        }

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

    private static final File DATA_FILE = new File("shadow-data.json");

    private static final Gson DATA_GSON;

    static {
        GsonBuilder builder = new GsonBuilder();
        DATA_GSON = builder.create();
    }

    AtomicReference<Deque<Thread>> saveThreadQueue = new AtomicReference<>(new ArrayDeque<>());

    public void saveAsync() {
        GameState stateCopy = this.state.clone();
        Thread thread = new Thread(() -> {
            try {
                FileWriter writer = new FileWriter(DATA_FILE);
                DATA_GSON.toJson(
                        stateCopy,
                        GameState.class,
                        writer
                );
                writer.close();
            } catch (IOException e) {
                LogUtils.getLogger().error("Error while saving data async", e);
            }
            this.saveThreadQueue.getAndUpdate((deque) -> {
                Thread nextThread = deque.pollFirst();
                if(nextThread != null) {
                    nextThread.start();
                }
                return deque;
            });
        });
        this.saveThreadQueue.getAndUpdate((deque) -> {
            if(deque.isEmpty()) {
                thread.start();
            } else {
                deque.add(thread);
            }
            return deque;
        });
    }

    public void saveSync() throws IOException {
        FileWriter writer = new FileWriter(DATA_FILE);
        DATA_GSON.toJson(
            this.state,
            GameState.class,
            writer
        );
        writer.close();
    }

    public void loadSync() throws IOException {
        FileReader reader = new FileReader(DATA_FILE);
        this.state = DATA_GSON.fromJson(
                reader,
                GameState.class
        );
        reader.close();
    }

    public void removeTickables(Class<? extends Tickable> tickableClass) {
        this.tickables.removeIf((tickable -> tickable.getClass() == tickableClass));
    }
}
