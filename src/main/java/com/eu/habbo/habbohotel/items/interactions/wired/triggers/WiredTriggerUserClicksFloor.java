package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WiredTriggerUserClicksFloor extends InteractionWiredTrigger {
    public static final WiredTriggerType type = WiredTriggerType.STATE_CHANGED;
    private final List<Integer> itemIds = new ArrayList<>();

    public WiredTriggerUserClicksFloor(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerUserClicksFloor(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public WiredTriggerType getType() {
        return type;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (stuff != null && stuff.length > 0 && stuff[0] instanceof HabboItem) {
            HabboItem itemClicado = (HabboItem) stuff[0];
            return this.itemIds.contains(itemClicado.getId());
        }
        return false;
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        this.itemIds.clear();
        for (int id : settings.getFurniIds()) {
            this.itemIds.add(id);
        }
        return true;
    }

    // ESTE É O MÉTODO QUE ESTAVA A FALTAR:
    @Override
    public String getWiredData() {
        // Transformamos a lista de IDs numa estrutura JSON que o emulador entende
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.itemIds));
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(15); // Limite de mobis selecionáveis
        message.appendInt(this.itemIds.size());

        for (int id : this.itemIds) {
            message.appendInt(id);
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
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.itemIds.clear();
        String wiredData = set.getString("wired_data");

        if (wiredData != null && wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            if (data != null && data.itemIds != null) {
                this.itemIds.addAll(data.itemIds);
            }
        }
    }

    @Override
    public void onPickUp() {
        this.itemIds.clear();
    }

    // Classe auxiliar para converter para JSON
    static class JsonData {
        List<Integer> itemIds;
        public JsonData(List<Integer> itemIds) { this.itemIds = itemIds; }
    }
}