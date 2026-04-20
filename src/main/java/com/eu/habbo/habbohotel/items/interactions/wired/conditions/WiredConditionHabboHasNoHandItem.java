package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionHabboHasNoHandItem extends InteractionWiredCondition {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredConditionHabboHasNoHandItem.class);

    // Use o tipo ACTOR_HAS_NO_HANDITEM no seu enum WiredConditionType
    public static final WiredConditionType type = WiredConditionType.ACTOR_HAS_NO_HANDITEM;

    private int handItem;

    public WiredConditionHabboHasNoHandItem(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionHabboHasNoHandItem(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Serializando Negativa de Item de Mão. ID Atual salvo: " + this.handItem);
        message.appendBoolean(false);
        message.appendInt(5);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(1);
        message.appendInt(this.handItem); // Mostra o item que ele NÃO deve ter
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        if(settings.getIntParams().length < 1) return false;

        this.handItem = settings.getIntParams()[0];
        System.out.println("[WIRED SAVE] Condição negativa salva! O Habbo NÃO pode ter o item: " + this.handItem);
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        // ESTE LOG TEM QUE APARECER NO CONSOLE
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("[CONDICAO NEGATIVA] Verificando item de mao...");

        if (roomUnit == null) return false;

        int itemNaMao = roomUnit.getHandItem();
        int itemProibido = this.handItem;

        // Log para compararmos os números
        System.out.println("[DEBUG] Proibido: " + itemProibido + " | Na Mao: " + itemNaMao);

        // Se o item que eu tenho na mão for IGUAL ao proibido, eu RETORNO FALSE (bloqueia)
        if (itemNaMao == itemProibido) {
            System.out.println("[RESULTADO] Bloqueado! Voce tem o item proibido.");
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return false;
        }

        System.out.println("[RESULTADO] Permitido! Voce nao tem o item proibido.");
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.handItem));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        try {
            String wiredData = set.getString("wired_data");
            System.out.println("[WIRED LOAD] Carregando negativa de item. Dados: " + wiredData);

            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.handItem = data.handItemId;
            } else if (!wiredData.isEmpty()) {
                this.handItem = Integer.parseInt(wiredData);
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }

    @Override
    public void onPickUp() {
        this.handItem = 0;
    }

    static class JsonData {
        int handItemId;
        public JsonData(int handItemId) {
            this.handItemId = handItemId;
        }
    }
}