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

public class WiredConditionHabboDirectionMatch extends InteractionWiredCondition {
    // Usamos o tipo nativo para que a SWF mostre as setas de direção
    public static final WiredConditionType type = WiredConditionType.ACTOR_DIRECTION;

    private int direction = 0;

    public WiredConditionHabboDirectionMatch(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionHabboDirectionMatch(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (roomUnit == null) return false;

        // Pegamos a rotação atual do corpo do Habbo (0 a 7)
        int currentRotation = roomUnit.getBodyRotation().getValue();

        // Comparamos com a direção salva no Wired
        return currentRotation == this.direction;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(0); // Não seleciona mobis
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");

        message.appendInt(1); // 1 parâmetro inteiro (a direção escolhida)
        message.appendInt(this.direction);

        message.appendInt(0);
        message.appendInt(this.getType().code); // Código que abre a interface de setas
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        // Quando clicas numa seta, o jogo envia o ID da direção no primeiro parâmetro
        if (settings.getIntParams().length >= 1) {
            this.direction = settings.getIntParams()[0];
        }
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.direction));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        try {
            if (wiredData != null && wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.direction = data.direction;
            }
        } catch (Exception e) {}
    }

    @Override
    public void onPickUp() {
        this.direction = 0;
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    static class JsonData {
        int direction;
        public JsonData(int direction) { this.direction = direction; }
    }
}