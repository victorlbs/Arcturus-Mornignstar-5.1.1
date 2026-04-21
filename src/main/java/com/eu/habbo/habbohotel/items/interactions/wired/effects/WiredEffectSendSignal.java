package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
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
import gnu.trove.set.hash.TIntHashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WiredEffectSendSignal extends InteractionWiredEffect {
    // Definimos o tipo (Certifique-se de ter essa ENUM ou use uma existente)
    private static final WiredEffectType type = WiredEffectType.CUSTOM;

    private boolean signalPerMobi = false;
    private boolean signalPerUser = false;
    private TIntHashSet selectedItems;

    public WiredEffectSendSignal(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.selectedItems = new TIntHashSet();
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        // Lógica de execução: Aqui você dispararia o evento de sinal
        // para os mobis contidos em this.selectedItems
        return true;
    }

    @Override
    public String getWiredData() {
        List<Integer> items = new ArrayList<>();
        for(int id : this.selectedItems.toArray()) items.add(id);

        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.signalPerMobi,
                this.signalPerUser,
                items,
                this.getDelay()
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.signalPerMobi = data.signalPerMobi;
            this.signalPerUser = data.signalPerUser;
            this.setDelay(data.delay);
            this.selectedItems.clear();
            this.selectedItems.addAll(data.items);
        }
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        // LOG DE DEPURAÇÃO NO CONSOLE DO INTELLIJ
        System.out.println("[WIRED DEBUG] Serializando Wired ID: " + this.getId() + " | Sprite: " + this.getBaseItem().getSpriteId());

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.selectedItems.size());
        for (int id : this.selectedItems.toArray()) message.appendInt(id);

        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(2); // Checkboxes
        message.appendInt(this.signalPerMobi ? 1 : 0);
        message.appendInt(this.signalPerUser ? 1 : 0);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
        message.appendInt(0);

        System.out.println("[WIRED DEBUG] Dados enviados para o cliente com sucesso.");
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("[WIRED DEBUG] Tentando salvar dados recebidos do Nitro...");

        try {
            if (settings.getIntParams().length < 2) {
                System.out.println("[WIRED DEBUG] Erro: Faltam parâmetros IntParams (Checkboxes).");
                return false;
            }

            this.signalPerMobi = settings.getIntParams()[0] == 1;
            this.signalPerUser = settings.getIntParams()[1] == 1;
            this.setDelay(settings.getDelay());

            this.selectedItems.clear();
            for (int id : settings.getFurniIds()) {
                this.selectedItems.add(id);
            }

            System.out.println("[WIRED DEBUG] Salvo com sucesso: SignalMobi=" + this.signalPerMobi + " | Delay=" + this.getDelay());
            return true;
        } catch (Exception e) {
            System.out.println("[WIRED DEBUG] Falha crítica no salvamento: " + e.getMessage());
            throw new WiredSaveException("Erro interno ao salvar.");
        }
    }


    @Override
    public WiredEffectType getType() { return type; }

    @Override
    public void onPickUp() {
        this.selectedItems.clear();
        this.signalPerMobi = false;
        this.signalPerUser = false;
        this.setDelay(0);
    }

    // Classe para persistência JSON igual ao seu exemplo
    static class JsonData {
        boolean signalPerMobi;
        boolean signalPerUser;
        List<Integer> items;
        int delay;

        public JsonData(boolean signalPerMobi, boolean signalPerUser, List<Integer> items, int delay) {
            this.signalPerMobi = signalPerMobi;
            this.signalPerUser = signalPerUser;
            this.items = items;
            this.delay = delay;
        }
    }
}