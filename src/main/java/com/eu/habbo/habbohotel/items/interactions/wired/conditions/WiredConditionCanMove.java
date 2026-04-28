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

public class WiredConditionCanMove extends InteractionWiredCondition {
    // Usamos o tipo de direção para a SWF abrir as setinhas de escolha
    public static final WiredConditionType type = WiredConditionType.ACTOR_DIRECTION;

    private int direction = 0;

    public WiredConditionCanMove(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionCanMove(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (roomUnit == null || room == null) return false;

        short checkX = roomUnit.getX();
        short checkY = roomUnit.getY();

        // Se a direção for 0 (nenhuma selecionada no wired), usamos a direção atual que o Habbo está a olhar
        int moveDir = (this.direction > 0) ? this.direction : roomUnit.getBodyRotation().getValue();

        // Calculamos a coordenada alvo com base na direção
        if (moveDir == 1) checkY--;      // Norte (Cima)
        else if (moveDir == 2) checkX++; // Leste (Direita)
        else if (moveDir == 3) checkY++; // Sul (Baixo)
        else if (moveDir == 4) checkX--; // Oeste (Esquerda)
        else if (moveDir == 5) { checkX--; checkY--; } // Diagonais (opcional, dependendo do emulador)
        else if (moveDir == 6) { checkX++; checkY--; }
        else if (moveDir == 7) { checkX++; checkY++; }
        else if (moveDir == 8) { checkX--; checkY++; } // Nota: O Habbo nativo usa 0-7, ajusta conforme a interface!

        // A mágica: Retorna verdadeiro apenas se o quadrado alvo puder ser pisado
        return room.tileWalkable(checkX, checkY);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(5);
        message.appendInt(0); // Não seleciona mobis
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");

        message.appendInt(1); // 1 Parâmetro (a direção selecionada)
        message.appendInt(this.direction);

        message.appendInt(0);
        message.appendInt(this.getType().code); // Código da interface de setinhas
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
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