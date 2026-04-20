package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectVariableText extends InteractionWiredEffect {
    // Certifica-te que VARIABLE_TEXT existe no teu WiredEffectType
    public static final WiredEffectType type = WiredEffectType.VARIABLE_TEXT;

    protected String message = "";

    public WiredEffectVariableText(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectVariableText(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Serializando Texto Variável. ID: " + this.getId());

        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());

        // A String da mensagem
        message.appendString(this.message);

        message.appendInt(0); // Parâmetros inteiros
        message.appendInt(0); // Opção selecionada

        // Tenta usar o código do SHOW_MESSAGE aqui se o VARIABLE_TEXT não funcionar
        message.appendInt(WiredEffectType.SHOW_MESSAGE.code);

        message.appendInt(this.getDelay());
        message.appendInt(0);
    }
    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("============== WIRED SAVE DEBUG ==============");
        String input = settings.getStringParam();
        System.out.println("[INFO] Texto digitado pelo usuário: " + input);

        if (gameClient.getHabbo() == null || !gameClient.getHabbo().hasPermission(Permission.ACC_SUPERWIRED)) {
            input = Emulator.getGameEnvironment().getWordFilter().filter(input, null);
            int maxLength = Emulator.getConfig().getInt("hotel.wired.message.max_length", 100);
            if (input.length() > maxLength) {
                input = input.substring(0, maxLength);
            }
        }

        this.message = input;
        this.setDelay(settings.getDelay());

        System.out.println("[RESULTADO] Mensagem salva: " + this.message);
        System.out.println("==============================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== WIRED EXECUTE DEBUG ==============");
        if (roomUnit == null || this.message.isEmpty()) {
            System.out.println("[!] Execução cancelada: Usuário nulo ou mensagem vazia.");
            return false;
        }

        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            // Processamento das etiquetas {}
            String processed = this.message
                    .replace("{username}", habbo.getHabboInfo().getUsername())
                    .replace("{credits}", habbo.getHabboInfo().getCredits() + "")
                    .replace("{online}", Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "");

            System.out.println("[Original] " + this.message);
            System.out.println("[Processado] " + processed);

            // Ação de exemplo: sussurrar a variável processada
            habbo.whisper(processed);

            System.out.println("[RESULTADO] Sucesso.");
            System.out.println("=================================================");
            return true;
        }
        return false;
    }

    @Override
    public String getWiredData() {
        // Usando o método getGsonBuilder() que vimos no teu WiredHandler!
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.message, this.getDelay()));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        System.out.println("[WIRED LOAD] Carregando dados: " + wiredData);

        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.message = data.message;
            this.setDelay(data.delay);
        }
    }

    @Override
    public void onPickUp() {
        this.message = "";
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    static class JsonData {
        String message;
        int delay;
        public JsonData(String message, int delay) {
            this.message = message;
            this.delay = delay;
        }
    }
}