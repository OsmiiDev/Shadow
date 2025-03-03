package com.maximumg9.shadow.roles;

import com.maximumg9.shadow.Shadow;
import com.maximumg9.shadow.ducks.ShadowProvider;
import com.maximumg9.shadow.util.IndirectPlayer;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.WrittenBookContentComponent;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.WrittenBookItem;
import net.minecraft.network.packet.s2c.play.OpenWrittenBookS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.RawFilteredPair;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import java.util.*;

public class RoleManager {

    private final RoleSlot[] roleSlots;

    private transient final Shadow shadow;

    public RoleManager(Shadow shadow) {
        roleSlots = new RoleSlot[shadow.config.roleSlotCount];

        for (int i = 0; i < roleSlots.length; i++) {
            roleSlots[i] = new RoleSlot(i);
        }
        this.shadow = shadow;
    }

    public RoleSlot getSlot(int id) {
        return roleSlots[id];
    }

    private static void openBook(ServerPlayerEntity player, ItemStack book) {
        if(book.getItem() instanceof WrittenBookItem) {
            PlayerInventory inventory = player.getInventory();

            ItemStack originalItem = inventory.getMainHandStack();

            int slot = inventory.main.size() + inventory.selectedSlot;

            int sync = player.currentScreenHandler.syncId;

            player.networkHandler.sendPacket(
                new ScreenHandlerSlotUpdateS2CPacket(
                    0,
                    sync,
                    slot,
                    book
                )
            );
            player.networkHandler.sendPacket(
                new OpenWrittenBookS2CPacket(
                    Hand.MAIN_HAND
                )
            );
            player.networkHandler.sendPacket(
                new ScreenHandlerSlotUpdateS2CPacket(
                    0,
                    sync,
                    slot,
                    originalItem
                )
            );
        } else {
            ((ShadowProvider) Objects.requireNonNull(player.getServer())).shadow$getShadow().ERROR("Could not open book for player:" + player.getNameForScoreboard());
        }
    }

    public void clearRoles() {
        shadow.getAllPlayers().forEach(
            (player) ->
            player.role = null
        );
    }

    public boolean pickRoles() {
        clearRoles();

        List<IndirectPlayer> onlinePlayers = shadow.getOnlinePlayers();

        if(onlinePlayers.size() > this.roleSlots.length) {
            shadow.ERROR("More players than role slots, consider increasing the number of role slots in the config");
            return false;
        }

        for (int i = 0; i < onlinePlayers.size(); i++) {
            IndirectPlayer player = onlinePlayers.get(i);

            player.role = this.roleSlots[i]
                .pickRandomRole(shadow.random)
                .factory.makeRole(shadow, player);
        }
        return true;
    }

    public void showRoleBook(ServerPlayerEntity player) {
        ItemStack book = new ItemStack(Items.WRITTEN_BOOK);

        ArrayList<RawFilteredPair<Text>> pages = new ArrayList<>(Arrays.stream(this.roleSlots).map((slot) -> {
            Text slotText = slot.getText();

            return new RawFilteredPair<>(slotText, Optional.empty());
        }).toList());

        Text addSlot = Text.literal("");

        pages.add(new RawFilteredPair<>(addSlot, Optional.empty()));

        book.set(
            DataComponentTypes.WRITTEN_BOOK_CONTENT,
            new WrittenBookContentComponent(
                    new RawFilteredPair<>("title", Optional.empty()),
                    "author",
                    0,
                    pages,
                    true
            )
        );

        openBook(player,book);
    }

}
