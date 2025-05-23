package com.maximumg9.shadow.config;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.roles.RoleSlot;
import com.maximumg9.shadow.screens.DecisionScreenHandler;
import com.maximumg9.shadow.screens.RoleSlotScreenHandler;
import com.maximumg9.shadow.util.indirectplayer.IndirectPlayer;
import com.mojang.logging.LogUtils;
import net.minecraft.nbt.*;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.*;

public class RoleManager {
    private final RoleSlot[] roleSlots;

    private final Shadow shadow;

    public RoleManager(Shadow shadow, Config config) {
        roleSlots = new RoleSlot[config.roleSlotCount];

        for (int i = 0; i < roleSlots.length; i++) {
            roleSlots[i] = new RoleSlot(i);
        }
        this.shadow = shadow;
    }

    void readNbt(NbtCompound nbt) {
        NbtList list = nbt.getList("roleSlots", NbtElement.COMPOUND_TYPE);

        int length = Math.min(list.size(), roleSlots.length);

        for(int i=0; i<length; i++) {
            this.roleSlots[i].readNbt(list.getCompound(i));
        }
        for(int i=length; i< roleSlots.length; i++) {
            this.roleSlots[i].reset();
        }
    }

    NbtCompound writeNbt(NbtCompound nbt) {
        NbtList list = new NbtList();
        list.addAll(
            Arrays.stream(this.roleSlots)
                .map(
                    (slot) -> slot.writeNbt(new NbtCompound())
                ).toList()
        );

        nbt.put("roleSlots", list);

        return nbt;
    }

    public RoleSlot getSlot(int id) {
        return roleSlots[id];
    }

    public void clearRoles() {
        shadow.getAllPlayers().forEach(
            (player) ->
            player.role = null
        );
    }

    public boolean pickRoles() {
        clearRoles();

        List<IndirectPlayer> onlinePlayers = new ArrayList<>(
                shadow.getOnlinePlayers().stream()
                        .filter((player -> player.participating))
                        .unordered()
                        .toList()
        );

        Collections.shuffle(onlinePlayers);

        if(onlinePlayers.size() > this.roleSlots.length) {
            shadow.ERROR("More players than role slots, consider increasing the number of role slots in the config");
            return false;
        }

        for (int i = 0; i < onlinePlayers.size(); i++) {
            IndirectPlayer player = onlinePlayers.get(i);

            player.role = this.roleSlots[i]
                .pickRandomRole(shadow.random)
                .factory.makeRole(player);
        }
        return true;
    }

    public void showRoleListIndex(ServerPlayerEntity player, boolean editable) {
        player.openHandledScreen(new DecisionScreenHandler.Factory<>(
                Text.literal("Pick a Role Slot to edit"),
                editable ? this::openRoleMenu : (slot, clicker) -> {},
                Arrays.asList(this.roleSlots)
        ));
    }

    private void openRoleMenu(RoleSlot slot, ServerPlayerEntity clicker) {
        if(slot == null) return;

        clicker.openHandledScreen(new RoleSlotScreenHandler.Factory(
                slot.getName(),
                slot
        ));

        LogUtils.getLogger().info(slot.toString());
    }

}
