package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredTriggerAtCounterTime extends InteractionWiredTrigger {
    private static final WiredTriggerType type = WiredTriggerType.CUSTOM;

    private THashSet<Integer> itemIds;
    private int targetTime = 0;
    private int selectionMode = 0; // 0 = Apenas mobis selecionados, 1 = Qualquer mobi

    public WiredTriggerAtCounterTime(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.itemIds = new THashSet<>();
    }

    public WiredTriggerAtCounterTime(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.itemIds = new THashSet<>();
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (stuff.length >= 2 && stuff[0] instanceof Integer && stuff[1] instanceof HabboItem) {
            int currentTime = (Integer) stuff[0];
            HabboItem counterItem = (HabboItem) stuff[1];

            if (currentTime == this.targetTime) {
                // Se o modo for "Qualquer mobi" (1)
                if (this.selectionMode == 1) {
                    return true;
                }
                // Se o modo for "Apenas selecionados" (0)
                return this.itemIds.contains(counterItem.getId());
            }
        }
        return false;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        this.itemIds.removeIf(id -> room.getHabboItem(id) == null);

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.itemIds.size());
        for (int id : this.itemIds) {
            message.appendInt(id);
        }
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");

        // Aumentamos para 3 inteiros para liberar a opção de seleção na UI
        message.appendInt(3);
        message.appendInt(this.targetTime / 60); // Minutos
        message.appendInt(this.targetTime % 60); // Segundos
        message.appendInt(this.selectionMode);   // Opção de modo (Mobi selecionado / Qualquer mobi)

        message.appendInt(0); // Seleção de Causa/Direção (se houver no sprite)
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        this.itemIds.clear();
        for (int id : settings.getFurniIds()) {
            this.itemIds.add(id);
        }

        if (settings.getIntParams().length >= 2) {
            this.targetTime = (settings.getIntParams()[0] * 60) + settings.getIntParams()[1];

            // Lê a nova opção de seleção se ela existir no pacote
            if (settings.getIntParams().length >= 3) {
                this.selectionMode = settings.getIntParams()[2];
            }
        }

        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.itemIds, this.targetTime, this.selectionMode
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.itemIds = data.itemIds;
            this.targetTime = data.targetTime;
            this.selectionMode = data.selectionMode;
        }
    }

    @Override
    public void onPickUp() {
        this.itemIds.clear();
        this.targetTime = 0;
        this.selectionMode = 0;
    }

    @Override
    public WiredTriggerType getType() {
        return type;
    }

    static class JsonData {
        THashSet<Integer> itemIds;
        int targetTime;
        int selectionMode;

        public JsonData(THashSet<Integer> itemIds, int targetTime, int selectionMode) {
            this.itemIds = itemIds;
            this.targetTime = targetTime;
            this.selectionMode = selectionMode;
        }
    }
}