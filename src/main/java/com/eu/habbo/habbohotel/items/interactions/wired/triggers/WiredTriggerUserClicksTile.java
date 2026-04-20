package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredTriggerUserClicksTile extends InteractionWiredTrigger {
    // Certifique-se de adicionar USER_CLICKS_TILE no seu enum WiredTriggerType
    public static final WiredTriggerType type = WiredTriggerType.USER_CLICKS_TILE;

    public WiredTriggerUserClicksTile(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerUserClicksTile(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG GATILHO CLIQUE NO PISO ==============");
        System.out.println("[INFO] O usuário clicou num quadrado do quarto!");

        // Passaremos o piso (RoomTile) clicado pelo Emulador. Isso é ótimo para o futuro
        // caso você queira fazer uma condição "Se clicou na coordenada X e Y".
        if (stuff.length > 0 && stuff[0] instanceof RoomTile) {
            RoomTile tile = (RoomTile) stuff[0];
            System.out.println("[INFO] Coordenada clicada: X=" + tile.x + ", Y=" + tile.y);
        }

        System.out.println("==========================================================");
        return true; // Continua a pilha
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Abrindo interface vazia do Gatilho Clicar no Piso.");
        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(0);
        message.appendInt(0);

        // Usa o código do tipo para tentar abrir. Se não abrir, pode trocar por 0.
        message.appendInt(this.getType().code);

        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        // Sem configurações complexas, apenas salva.
        return true;
    }

    @Override
    public String getWiredData() {
        return "";
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        // Nada a carregar
    }

    @Override
    public void onPickUp() {
        // Nada a limpar
    }

    @Override
    public WiredTriggerType getType() {
        return type;
    }

    @Override
    public boolean isTriggeredByRoomUnit() {
        return true;
    }
}