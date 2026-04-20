package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectMessageAll extends InteractionWiredEffect {
    // Você pode criar um MESSAGE_ALL no seu enum, mas para a interface de texto
    // abrir sem dor de cabeça, vamos emprestar a interface do SHOW_MESSAGE.
    public static final WiredEffectType type = WiredEffectType.SHOW_MESSAGE;

    private String message = "";

    public WiredEffectMessageAll(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectMessageAll(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Serializando Efeito: Mensagem para Todos. Msg atual: " + this.message);

        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());

        // Envia o texto salvo para a caixa de texto
        message.appendString(this.message);

        message.appendInt(0);
        message.appendInt(0);

        // Usamos o código da mensagem padrão para garantir que a SWF abra a caixa de texto
        message.appendInt(type.code);

        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("============== DEBUG SAVE MENSAGEM GLOBAL ==============");
        String input = settings.getStringParam();

        if (gameClient.getHabbo() == null || !gameClient.getHabbo().hasPermission(Permission.ACC_SUPERWIRED)) {
            input = Emulator.getGameEnvironment().getWordFilter().filter(input, null);
            int maxLength = Emulator.getConfig().getInt("hotel.wired.message.max_length", 100);
            if (input.length() > maxLength) {
                input = input.substring(0, maxLength);
            }
        }

        this.message = input;
        this.setDelay(settings.getDelay());

        System.out.println("[RESULTADO] Mensagem Global salva: " + this.message);
        System.out.println("========================================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE MENSAGEM GLOBAL ==============");
        if (this.message.isEmpty() || room == null) {
            System.out.println("[!] Cancelado: Mensagem vazia ou quarto nulo.");
            return false;
        }

        System.out.println("[INFO] Enviando mensagem global para todos os usuários do quarto!");

        int count = 0;

        // O SEGREDO: Em vez de pegar apenas um Habbo, pegamos TODOS os Habbos do quarto
        for (Habbo h : room.getHabbos()) {
            if (h != null && h.getRoomUnit() != null) {

                // Processamos as variáveis individualmente para cada usuário!
                // Assim, se a mensagem for "Olá {username}", cada um vê o seu próprio nome.
                String processedMsg = this.message
                        .replace("{username}", h.getHabboInfo().getUsername())
                        .replace("{credits}", String.valueOf(h.getHabboInfo().getCredits()));

                // Envia como um sussurro amarelo do Wired para o usuário
                h.getClient().sendResponse(new RoomUserWhisperComposer(
                        new RoomChatMessage(processedMsg, h, h, RoomChatMessageBubbles.WIRED)
                ));
                count++;
            }
        }

        System.out.println("[RESULTADO] Mensagem enviada para " + count + " usuários.");
        System.out.println("===========================================================");
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.message, this.getDelay()));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.message = data.message;
                this.setDelay(data.delay);
            } else if (!wiredData.isEmpty()) {
                String[] data = wiredData.split("\t");
                if (data.length >= 2) {
                    this.setDelay(Integer.parseInt(data[0]));
                    this.message = data[1];
                }
            }
        } catch (Exception e) {
            System.out.println("[!] Erro ao carregar dados do Wired Mensagem Global.");
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

    @Override
    public boolean requiresTriggeringUser() {
        // Pode ser falso! Se for ativado por um "Repetidor" (timer), ele ainda envia para todos
        return false;
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