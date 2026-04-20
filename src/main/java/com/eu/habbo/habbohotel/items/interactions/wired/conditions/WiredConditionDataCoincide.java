package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionDataCoincide extends InteractionWiredCondition {
    // Usamos CUSTOM ou o código específico para "Sinal Coincide"
    public static final WiredConditionType type = WiredConditionType.CUSTOM;

    private String expectedData = "";

    public WiredConditionDataCoincide(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionDataCoincide(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        // Se algo foi passado no sinal (stuff) e for uma String...
        if (stuff != null && stuff.length > 0 && stuff[0] instanceof String) {
            String receivedSignal = (String) stuff[0];
            // Retorna verdadeiro se o sinal recebido for igual ao configurado (ignora maiúsculas/minúsculas)
            return receivedSignal.equalsIgnoreCase(this.expectedData);
        }
        return false;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.expectedData
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");

        try {
            if (wiredData != null && wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.expectedData = data.expectedData;
            } else {
                // Suporte para dados antigos (formato texto simples)
                this.expectedData = wiredData != null ? wiredData : "";
            }
        } catch (Exception e) {
            this.expectedData = "";
        }
    }

    @Override
    public void onPickUp() {
        this.expectedData = "";
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(5); // Limite visual de mobis (mesmo sem usar nenhum)
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString(this.expectedData); // O texto que aparece na caixa do Wired
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        // Diferente do seu exemplo que usa getIntParams, aqui usamos getStringParam
        this.expectedData = settings.getStringParam();
        return true;
    }

    static class JsonData {
        String expectedData;

        public JsonData(String expectedData) {
            this.expectedData = expectedData;
        }
    }
}