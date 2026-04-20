package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.DanceType;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionHabboAction extends InteractionWiredCondition {
    private static final WiredConditionType type = WiredConditionType.ACTOR_DOES_ACTION;

    protected int actionId;

    public WiredConditionHabboAction(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionHabboAction(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (roomUnit == null) return false;

        System.out.println("============== WIRED EXECUTE DEBUG ==============");
        System.out.println("[Configurado] Esperando ID: " + this.actionId);

        boolean success = false;

        // No teu emulador/SWF, os IDs parecem estar trocados ou fixos em 0.
        // Vamos mapear de acordo com o que o console mostrou:
        switch (this.actionId) {
            case 0:
                // Se o ID for 0, vamos aceitar tanto o Aceno (Handitem 1) quanto a Dança
                // para garantir que funcione enquanto a interface envia 0
                if (roomUnit.getHandItem() == 1 || (roomUnit.getDanceType() != null && roomUnit.getDanceType() != DanceType.NONE)) {
                    success = true;
                }
                break;

            case 4: // Caso a SWF envie o ID 4 para dança
                success = (roomUnit.getDanceType() != null && roomUnit.getDanceType() != DanceType.NONE);
                break;

            case 5: // Caso seja sentar
                success = (roomUnit.hasStatus(RoomUnitStatus.SIT) || roomUnit.cmdSit);
                break;
        }

        System.out.println("[Habbo] HandItem: " + roomUnit.getHandItem());
        System.out.println("[Habbo] Dança: " + (roomUnit.getDanceType() != null ? roomUnit.getDanceType().name() : "NONE"));
        System.out.println("[Resultado] " + (success ? "ACEITO (Teleportando...)" : "RECUSADO"));
        System.out.println("=================================================");

        return success;
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        if (settings.getIntParams().length < 1) {
            System.out.println("[WIRED SAVE] Erro: Nenhum parâmetro recebido ao salvar!");
            return false;
        }

        this.actionId = settings.getIntParams()[0];

        // --- DEBUG LOG ---
        System.out.println("[WIRED SAVE] actionId salvo com sucesso: " + this.actionId);

        return true;
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");

        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.actionId = data.actionId;
        } else if (!wiredData.isEmpty()) {
            this.actionId = Integer.parseInt(wiredData);
        }

        // --- DEBUG LOG ---
        System.out.println("[WIRED LOAD] Dados carregados da DB. actionId: " + this.actionId);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(1);
        message.appendInt(this.actionId);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);

        // --- DEBUG LOG ---
        System.out.println("[WIRED SERIALIZE] Enviando actionId " + this.actionId + " para a interface do cliente.");
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.actionId));
    }

    @Override
    public void onPickUp() {
        this.actionId = 0;
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    static class JsonData {
        int actionId;
        public JsonData(int actionId) { this.actionId = actionId; }
    }
}