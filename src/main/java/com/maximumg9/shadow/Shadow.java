package com.maximumg9.shadow;

import com.maximumg9.shadow.abilities.NetherStarItem;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class Shadow {

    public static final Map<Identifier, Function<Item.Settings,Item>> modifiedItems = new HashMap<>();

    static {
        modifiedItems.put(Identifier.ofVanilla("nether_star"), NetherStarItem::new);
    }

    private final MinecraftServer server;

    public Shadow(MinecraftServer server) {
        this.server = server;
    }

    public MinecraftServer getServer() {return this.server;}
}
