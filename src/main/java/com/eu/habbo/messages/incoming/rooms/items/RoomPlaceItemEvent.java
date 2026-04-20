package com.eu.habbo.messages.incoming.rooms.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.FurnitureType;
import com.eu.habbo.habbohotel.items.interactions.*;
import com.eu.habbo.habbohotel.modtool.ScripterManager;
import com.eu.habbo.habbohotel.rooms.*;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertKeys;
import com.eu.habbo.messages.outgoing.inventory.RemoveHabboItemComposer;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;

public class RoomPlaceItemEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        String[] values = this.packet.readString().split(" ");
        int itemId = -1;

        if (values.length != 0) itemId = Integer.parseInt(values[0]);

        if (!this.client.getHabbo().getRoomUnit().isInRoom()) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.NO_RIGHTS.errorCode));
            return;
        }

        Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) return;

        HabboItem item = this.client.getHabbo().getInventory().getItemsComponent().getHabboItem(itemId);

        if (item == null || item.getBaseItem().getInteractionType().getType() == InteractionPostIt.class)
            return;

        if (room.getId() != item.getRoomId() && item.getRoomId() != 0)
            return;

        // Verificações de limites (Dimmers/Jukebox)
        if (item instanceof InteractionMoodLight && !room.getRoomSpecialTypes().getItemsOfType(InteractionMoodLight.class).isEmpty()) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.MAX_DIMMERS.errorCode));
            return;
        }
        if (item instanceof InteractionJukeBox && !room.getRoomSpecialTypes().getItemsOfType(InteractionJukeBox.class).isEmpty()) {
            this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, FurnitureMovementError.MAX_SOUNDFURNI.errorCode));
            return;
        }

        // LÓGICA PARA MOBIS DE CHÃO (FLOOR)
        if (item.getBaseItem().getType() == FurnitureType.FLOOR) {
            short x = Short.parseShort(values[1]);
            short y = Short.parseShort(values[2]);
            int rotation = Integer.parseInt(values[3]);

            RoomTile tile = room.getLayout().getTile(x, y);
            if (tile == null) return;

            // Tenta colocar o primeiro item (o original)
            FurnitureMovementError error = room.canPlaceFurnitureAt(item, this.client.getHabbo(), tile, rotation);
            if (!error.equals(FurnitureMovementError.NONE)) {
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, error.errorCode));
                return;
            }

            error = room.placeFloorFurniAt(item, tile, rotation, this.client.getHabbo());
            if (!error.equals(FurnitureMovementError.NONE)) {
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, error.errorCode));
                return;
            }

            // ============================================================
            // INÍCIO DA LÓGICA DO MULTIPLICADOR
            // ============================================================
            if (this.client.getHabbo().getHabboStats().cache.containsKey("multiplace_amount")) {
                int amount = (int) this.client.getHabbo().getHabboStats().cache.get("multiplace_amount");

                for (int i = 1; i < amount; i++) {
                    // Cria uma nova instância na DB
                    HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItem(
                            this.client.getHabbo().getHabboInfo().getId(),
                            item.getBaseItem(),
                            0, 0, ""
                    );

                    if (newItem != null) {
                        newItem.setX(item.getX());
                        newItem.setY(item.getY());
                        newItem.setRotation(item.getRotation());

                        // Calcula altura Z (empilhamento)
                        double stackHeight = i * item.getBaseItem().getHeight();
                        newItem.setZ(item.getZ() + stackHeight);

                        newItem.setRoomId(room.getId());
                        room.addHabboItem(newItem);
                        newItem.needsUpdate(true);

                        // Envia para os outros verem
                        room.sendComposer(new AddFloorItemComposer(newItem, this.client.getHabbo().getHabboInfo().getUsername()).compose());
                    }
                }
            }
            // ============================================================
            // FIM DA LÓGICA DO MULTIPLICADOR
            // ============================================================

        } else {
            // LÓGICA PARA MOBIS DE PAREDE (WALL)
            FurnitureMovementError error = room.placeWallFurniAt(item, values[1] + " " + values[2] + " " + values[3], this.client.getHabbo());
            if (!error.equals(FurnitureMovementError.NONE)) {
                this.client.sendResponse(new BubbleAlertComposer(BubbleAlertKeys.FURNITURE_PLACEMENT_ERROR.key, error.errorCode));
                return;
            }
        }

        // Remove o item original do inventário
        this.client.sendResponse(new RemoveHabboItemComposer(item.getGiftAdjustedId()));
        this.client.getHabbo().getInventory().getItemsComponent().removeHabboItem(item.getId());
        item.setFromGift(false);
    }
}