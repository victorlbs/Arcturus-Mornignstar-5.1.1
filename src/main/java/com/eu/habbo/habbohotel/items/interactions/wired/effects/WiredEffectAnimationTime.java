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

public class WiredEffectAnimationTime extends InteractionWiredEffect {
    // Adicione ANIMATION_TIME no seu enum WiredEffectType (ou use um ID livre)
    public static final WiredEffectType type = WiredEffectType.ANIMATION_TIME;

    private int animationTime = 0;

    public WiredEffectAnimationTime(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectAnimationTime(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Serializando EFEITO de Animação. Tempo salvo: " + this.animationTime);

        message.appendBoolean(false); // Não seleciona mobis no quarto
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");

        // Quantidade de parâmetros inteiros e o próprio parâmetro (o tempo do slider)
        message.appendInt(1);
        message.appendInt(this.animationTime);

        message.appendInt(0);
        message.appendInt(0);

        // Como é um EFEITO, ele tem a opção de Delay
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("============== DEBUG SAVE EFEITO ANIMAÇÃO ==============");

        if (settings.getIntParams().length < 1) {
            System.out.println("[!] Erro: Nenhum parâmetro de tempo recebido da SWF.");
            return false;
        }

        // Salva o valor escolhido no slider de tempo de animação
        this.animationTime = settings.getIntParams()[0];

        // Salva o valor do slider de Delay (padrão de todos os Efeitos)
        this.setDelay(settings.getDelay());

        System.out.println("[RESULTADO] Tempo de animação salvo: " + this.animationTime);
        System.out.println("[RESULTADO] Delay do efeito salvo: " + this.getDelay());
        System.out.println("========================================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE EFEITO ANIMAÇÃO ==============");
        System.out.println("[INFO] O efeito de tempo de animação foi ativado na pilha.");
        System.out.println("[INFO] Tempo configurado no Wired: " + this.animationTime);

        // Lógica de implementação:
        // Como este Wired em si não move nada, você pode usar este execute
        // para salvar o 'animationTime' em uma variável temporária do quarto
        // para que o próximo Efeito (Mover) na pilha leia esse tempo.

        System.out.println("===========================================================");
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.animationTime, this.getDelay()));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");

        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.animationTime = data.time;
                this.setDelay(data.delay);
            } else if (!wiredData.isEmpty()) {
                // Legado: caso estivesse salvo apenas como número solto antes
                String[] data = wiredData.split(";");
                this.animationTime = Integer.parseInt(data[0]);
                if (data.length > 1) this.setDelay(Integer.parseInt(data[1]));
            }
            System.out.println("[WIRED LOAD] Efeito Animação carregado. Tempo: " + this.animationTime);
        } catch (Exception e) {
            System.out.println("[!] Erro ao carregar os dados do Wired Efeito Animação.");
        }
    }

    @Override
    public void onPickUp() {
        this.animationTime = 0;
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    // Método acessível para outros Wireds
    public int getAnimationTime() {
        return this.animationTime;
    }

    static class JsonData {
        int time;
        int delay;

        public JsonData(int time, int delay) {
            this.time = time;
            this.delay = delay;
        }
    }
}