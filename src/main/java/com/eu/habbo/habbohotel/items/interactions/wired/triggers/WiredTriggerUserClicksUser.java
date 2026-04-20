package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredTriggerUserClicksUser extends InteractionWiredTrigger {
    // Certifique-se de adicionar USER_CLICKS_USER no seu enum WiredTriggerType
    public static final WiredTriggerType type = WiredTriggerType.USER_CLICKS_USER;

    public WiredTriggerUserClicksUser(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerUserClicksUser(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG GATILHO CLIQUE ==============");
        System.out.println("[INFO] O gatilho de clicar em usuário foi ativado!");

        // Se quisermos validar quem clicou e quem foi clicado, podemos extrair do array 'stuff'
        // que enviaremos a partir do Emulador.

        System.out.println("==================================================");
        return true; // Retorna true para continuar e ativar os efeitos da pilha
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Abrindo interface vazia do Gatilho Clique.");
        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(0);
        message.appendInt(0);

        // Usamos o getType().code ou um código genérico (como o do ENTER_ROOM) para abrir vazio
        message.appendInt(this.getType().code);

        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        // Não precisamos salvar nada, é só clicar em "Pronto"
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
        return true; // Depende de um Habbo para ser ativado
    }
}