package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class WiredTriggerUserClicksTile extends InteractionWiredTrigger {
    public static final WiredTriggerType type = WiredTriggerType.USER_CLICKS_TILE;
    private THashSet<HabboItem> items; // Lista de mobis selecionados

    public WiredTriggerUserClicksTile(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.items = new THashSet<>();
    }

    public WiredTriggerUserClicksTile(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.items = new THashSet<>();
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        // 'stuff[0]' deve ser o RoomTile enviado pelo pacote de clique
        if (stuff.length >= 1 && stuff[0] instanceof RoomTile) {
            RoomTile clickedTile = (RoomTile) stuff[0];

            // Verifica se no local clicado existe um mobi que está na nossa lista de selecionados
            for (HabboItem item : this.items) {
                if (item.getX() == clickedTile.x && item.getY() == clickedTile.y) {
                    return true; // Clique validado em mobi da lista!
                }
            }
        }
        return false;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        // Limpa itens que não existem mais no quarto
        this.items.removeIf(item -> item.getRoomId() != this.getRoomId() || room.getHabboItem(item.getId()) == null);

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.items.size());
        for (HabboItem item : this.items) {
            message.appendInt(item.getId());
        }
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        this.items.clear();
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());

        if (room != null) {
            for (int itemId : settings.getFurniIds()) {
                HabboItem item = room.getHabboItem(itemId);
                if (item != null) this.items.add(item);
            }
        }
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.items.stream().map(HabboItem::getId).collect(Collectors.toList())
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            for (Integer id : data.itemIds) {
                HabboItem item = room.getHabboItem(id);
                if (item != null) this.items.add(item);
            }
        }
    }

    @Override
    public void onPickUp() { this.items.clear(); }

    @Override
    public WiredTriggerType getType() { return type; }

    @Override
    public boolean isTriggeredByRoomUnit() { return true; }

    static class JsonData {
        List<Integer> itemIds;
        public JsonData(List<Integer> itemIds) { this.itemIds = itemIds; }
    }
}