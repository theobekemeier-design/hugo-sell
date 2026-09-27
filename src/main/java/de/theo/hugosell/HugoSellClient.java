package de.theo.hugosell;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;

/**
 * Client-side helper for the Hugo SMP /sell GUI.
 *
 * The server still handles the actual selling. This mod only performs the
 * same inventory clicks a player could perform manually.
 */
public final class HugoSellClient implements ClientModInitializer {
    private static final String SELL_TITLE = "Items verkaufen";

    // Based on the supplied Hugo SMP screenshot:
    // 4 rows x 9 slots = 36 slots in the sell area.
    private static final int CONFIRM_SLOT = 35;

    // The player inventory follows the 36 sell slots in the container.
    private static final int PLAYER_INVENTORY_START = 36;
    private static final int PLAYER_INVENTORY_END = 72;

    private boolean armed;
    private int waitTicks;
    private int nextInventorySlot;
    private int confirmWaitTicks = -1;

    @Override
    public void onInitializeClient() {
        ClientSendMessageEvents.COMMAND.register(command -> {
            if (command.trim().equalsIgnoreCase("sell")) {
                armed = true;
                waitTicks = 0;
                nextInventorySlot = PLAYER_INVENTORY_START;
                confirmWaitTicks = -1;
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(Minecraft client) {
        if (!armed || client.level == null) {
            return;
        }

        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) {
            if (++waitTicks > 100) reset();
            return;
        }

        Component title = screen.getTitle();
        if (!SELL_TITLE.equals(title.getString())) {
            if (++waitTicks > 100) reset();
            return;
        }

        // Let the server/client finish synchronising the freshly opened menu.
        if (waitTicks < 3) {
            waitTicks++;
            return;
        }

        if (nextInventorySlot < PLAYER_INVENTORY_END) {
            Slot slot = screen.getMenu().getSlot(nextInventorySlot);
            if (slot != null && !slot.getItem().isEmpty()
                    && client.gameMode != null && client.player != null) {
                client.gameMode.handleInventoryMouseClick(
                        screen.getMenu().containerId,
                        nextInventorySlot,
                        0,
                        ClickType.QUICK_MOVE,
                        client.player
                );
            }
            nextInventorySlot++;
            return;
        }

        // Give the server one tick to process the last quick-move.
        if (confirmWaitTicks < 0) {
            confirmWaitTicks = 1;
            return;
        }
        if (confirmWaitTicks > 0) {
            confirmWaitTicks--;
            return;
        }

        if (client.gameMode != null && client.player != null) {
            client.gameMode.handleInventoryMouseClick(
                    screen.getMenu().containerId,
                    CONFIRM_SLOT,
                    0,
                    ClickType.PICKUP,
                    client.player
            );
        }

        reset();
    }

    private void reset() {
        armed = false;
        waitTicks = 0;
        nextInventorySlot = PLAYER_INVENTORY_START;
        confirmWaitTicks = -1;
    }
}
