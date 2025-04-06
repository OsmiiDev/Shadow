package com.maximumg9.shadow;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.maximumg9.shadow.abilities.NetherStarItem;
import com.maximumg9.shadow.commands.*;
import com.maximumg9.shadow.util.IndirectPlayer;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.logging.LogUtils;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import org.slf4j.Logger;

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
        RoleCommand.register(dispatcher);
        CancelCommand.register(dispatcher);
    }

    private final MinecraftServer server;
    public Config config = new Config(this);
    public final Random random = Random.create();
    private final List<Tickable> tickables = new ArrayList<>();
    private static final Logger LOGGER = LogUtils.getLogger();

    public GameState state = new GameState();

    public Shadow(MinecraftServer server) {
        this.server = server;

        try {
            this.loadSync();
        } catch (FileNotFoundException e) {
            LOGGER.warn("Failed to load data, creating file");
            this.state = new GameState();
        } catch (IOException e) {
            LOGGER.warn("Exception while loading data");
        }

        try {
            this.saveSync();
        } catch(IOException e) {
            LOGGER.warn("Failed to save data");
        }
    }

    public void ERROR(String message) {
        this.getServer().getPlayerManager().broadcast(Text.literal(message).styled((style) -> style.withColor(Formatting.RED)),false);
        LOGGER.error(message);
    }

    public List<IndirectPlayer> getAllPlayers() {
        getOnlinePlayers();
        return this.state.indirectPlayers.values().stream().toList();
    }

    public void clearEyes() {
        for(Eye eye : this.state.eyes) {
            eye.destroy(this);
        }
        this.state.eyes.clear();
    }

    public void cancelGame() {
        this.clearEyes();

        this.state = new GameState(this.state);

        for(IndirectPlayer player : getAllPlayers()) {
            player.role = null;
        }

        this.saveAsync();
    }

    public IndirectPlayer getIndirect(ServerPlayerEntity player) {
        return this.state.getIndirect(player);
    }

    public List<IndirectPlayer> getOnlinePlayers() {
        return this.server.getPlayerManager().getPlayerList().stream().map((player) -> this.state.getIndirect(player)).toList();
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
        LOGGER.info("Saving async...");

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
                LOGGER.error("Error while saving data async", e);
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
}
