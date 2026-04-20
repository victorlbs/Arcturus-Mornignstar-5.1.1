package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredTriggerUserPerformsAction extends InteractionWiredTrigger {
    // Certifique-se de adicionar USER_PERFORMS_ACTION no seu enum WiredTriggerType
    public static final WiredTriggerType type = WiredTriggerType.USER_PERFORMS_ACTION;

    // Vai guardar o ID da ação selecionada na interface (ex: 1=Acenar, 2=Beijo, 3=Rir...)
    private int actionId = 0;

    public WiredTriggerUserPerformsAction(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerUserPerformsAction(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Serializando Gatilho Ação. Ação salva: " + this.actionId);
        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");

        // Envia o parâmetro da ação escolhida para a SWF marcar a opção correta
        message.appendInt(1);
        message.appendInt(this.actionId);

        message.appendInt(0);

        // Tente usar o this.getType().code. Se a janela não abrir, mude para um código de gatilho com opções que a sua SWF conheça
        message.appendInt(14); // 14 é o código oficial da interface de Ação na SWF

        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        System.out.println("============== DEBUG SAVE GATILHO AÇÃO ==============");
        if (settings.getIntParams().length > 0) {
            this.actionId = settings.getIntParams()[0];
            System.out.println("[RESULTADO] Ação escolhida no Wired: " + this.actionId);
        } else {
            this.actionId = 0;
            System.out.println("[!] Nenhuma ação específica selecionada.");
        }
        System.out.println("=====================================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE GATILHO AÇÃO ==============");

        // Verificamos se o Emulador enviou qual ação o jogador fez
        if (stuff.length > 0 && stuff[0] instanceof Integer) {
            int performedAction = (Integer) stuff[0];
            System.out.println("[INFO] O usuário fez a ação ID: " + performedAction);
            System.out.println("[INFO] O Wired está configurado para a ação ID: " + this.actionId);

            // Verifica se a ação feita bate com a ação configurada no Wired
            // Se actionId for 0, geralmente significa "Qualquer Ação"
            if (this.actionId == performedAction || this.actionId == 0) {
                System.out.println("[RESULTADO] Ação confirmada! Ativando a pilha...");
                System.out.println("========================================================");
                return true;
            }
        }

        System.out.println("[RESULTADO] Ação errada. A pilha não será ativada.");
        System.out.println("========================================================");
        return false;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.actionId));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.actionId = data.actionId;
            } else if (!wiredData.isEmpty()) {
                this.actionId = Integer.parseInt(wiredData);
            }
        } catch (Exception e) {
            System.out.println("[!] Erro ao carregar dados do Wired Ação.");
        }
    }

    @Override
    public void onPickUp() {
        this.actionId = 0;
    }

    @Override
    public WiredTriggerType getType() {
        return type;
    }

    @Override
    public boolean isTriggeredByRoomUnit() {
        return true;
    }

    static class JsonData {
        int actionId;
        public JsonData(int actionId) {
            this.actionId = actionId;
        }
    }
}