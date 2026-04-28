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

public class WiredConditionHabboDirection extends InteractionWiredCondition {
    // Verifique se o seu emulador tem este tipo, caso contrário use CUSTOM
    // Tente usar o tipo nativo para direções
    public static final WiredConditionType type = WiredConditionType.ACTOR_DIRECTION;


    private int direction = 0;

    public WiredConditionHabboDirection(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionHabboDirection(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (roomUnit == null) return false;

        // ACESSO AO SEU ROOMUNIT:
        // Pegamos a rotação do corpo (bodyRotation) e convertemos para o valor numérico (int)
        return roomUnit.getBodyRotation().getValue() == this.direction;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.direction));
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false); // Seleção de mobi (false pois não usamos mobis aqui)
        message.appendInt(5);         // Limite visual
        message.appendInt(0);         // Quantidade de mobis selecionados
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");     // Nenhuma mensagem de texto

        // CONFIGURAÇÃO DA INTERFACE
        message.appendInt(1);         // Dizemos ao cliente: "Vou te enviar 1 parâmetro inteiro"
        message.appendInt(this.direction); // O parâmetro é a direção (0 a 7)

        message.appendInt(0);         // Delay (0)

        // O CÓDIGO DO TIPO (O "RG" DA INTERFACE)
        // Tente usar o número 7 ou 8. No Habbo nativo, 7 é direção.
        message.appendInt(7);

        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        if (settings.getIntParams().length < 1) return false;
        this.direction = settings.getIntParams()[0];
        return true;
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData != null && wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.direction = data.direction;
        }
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