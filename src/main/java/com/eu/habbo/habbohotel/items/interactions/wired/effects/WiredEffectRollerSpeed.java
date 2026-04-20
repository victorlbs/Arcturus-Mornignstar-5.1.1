package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectRollerSpeed extends InteractionWiredEffect {
    // Usamos o tipo original na sua estrutura
    public static final WiredEffectType type = WiredEffectType.ROLLER_SPEED;

    private int speed = 0;

    public WiredEffectRollerSpeed(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectRollerSpeed(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Abrindo Caixa de Velocidade. Atual: " + this.speed);

        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());

        // Enviamos o número atual como um TEXTO para a caixa
        message.appendString(String.valueOf(this.speed));

        message.appendInt(0);
        message.appendInt(0);

        // --- O SEGREDO ESTÁ AQUI ---
        // Forçamos o código do SHOW_MESSAGE para a SWF abrir a caixa de texto
        message.appendInt(WiredEffectType.SHOW_MESSAGE.code);
        // Se der erro no SHOW_MESSAGE.code, você pode digitar o número 7 diretamente

        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("============== DEBUG SAVE VELOCIDADE ROLLER ==============");
        String input = settings.getStringParam(); // Pega o que você digitou na caixa

        try {
            // Tenta transformar o texto digitado em número
            this.speed = Integer.parseInt(input);
            System.out.println("[INFO] Velocidade digitada: " + this.speed);

            // Trava de segurança (para ninguém colocar velocidade absurda e travar o quarto)
            if (this.speed < -1 || this.speed > 100) {
                this.speed = 0;
                System.out.println("[!] Velocidade fora do limite. Resetada para 0.");
            }
        } catch (NumberFormatException e) {
            System.out.println("[!] Erro: Você não digitou um número válido. Salvando como 0.");
            this.speed = 0;
        }

        this.setDelay(settings.getDelay());

        System.out.println("[RESULTADO] Salvo com sucesso! Velocidade: " + this.speed);
        System.out.println("==========================================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE VELOCIDADE ROLLER ==============");
        if (room == null) return false;

        System.out.println("[INFO] Aplicando velocidade aos rollers: " + this.speed);

        // Exatamente como no seu SetSpeedCommand
        room.setRollerSpeed(this.speed);
        room.setNeedsUpdate(true);

        System.out.println("[RESULTADO] Rollers atualizados.");
        System.out.println("=============================================================");
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.speed, this.getDelay()));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.speed = data.speed;
                this.setDelay(data.delay);
            } else if (!wiredData.isEmpty()) {
                String[] data = wiredData.split("\t");
                if (data.length >= 2) {
                    this.setDelay(Integer.parseInt(data[0]));
                    this.speed = Integer.parseInt(data[1]);
                }
            }
        } catch (Exception e) {
            System.out.println("[!] Erro ao carregar dados do Wired.");
        }
    }

    @Override
    public void onPickUp() {
        this.speed = 0;
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    static class JsonData {
        int speed;
        int delay;
        public JsonData(int speed, int delay) {
            this.speed = speed;
            this.delay = delay;
        }
    }
}