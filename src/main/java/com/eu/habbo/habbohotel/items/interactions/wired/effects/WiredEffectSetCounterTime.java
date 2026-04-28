package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.wired.WiredEffectDataComposer;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class WiredEffectSetCounterTime extends InteractionWiredEffect {
    // Certifique-se de que SET_COUNTER_TIME existe no seu WiredEffectType ou use o ID correto
    public static final WiredEffectType type = WiredEffectType.SET_COUNTER_TIME;

    private THashSet<Integer> itemIds;
    private int mode = 0; // 0: Aumentar, 1: Diminuir, 2: Configurar
    private int timeAmount = 0; // Total em segundos

    public WiredEffectSetCounterTime(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.itemIds = new THashSet<>();
    }

    public WiredEffectSetCounterTime(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.itemIds = new THashSet<>();
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        // Limpeza de itens removidos do quarto
        this.itemIds.removeIf(id -> room.getHabboItem(id) == null);

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.itemIds.size());
        for (int id : this.itemIds) {
            message.appendInt(id);
        }
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString(""); // String param (não usada aqui)

        // Dados para os sliders e radio buttons da sua print
        message.appendInt(3); // Quantidade de intParams
        message.appendInt(this.mode); // Aumentar/Diminuir/Configurar
        message.appendInt(this.timeAmount / 60); // Minutos no slider
        message.appendInt(this.timeAmount % 60); // Segundos no slider

        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public void onPickUp() {
        // Limpa a lista de mobis (contadores) selecionados
        this.itemIds.clear();

        // Reseta as variáveis para o padrão
        this.mode = 0;
        this.timeAmount = 0;
        this.setDelay(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        this.itemIds.clear();
        for (int id : settings.getFurniIds()) {
            this.itemIds.add(id);
        }

        if (settings.getIntParams().length < 3) {
            throw new WiredSaveException("Dados insuficientes para configurar o tempo.");
        }

        this.mode = settings.getIntParams()[0];
        int minutes = settings.getIntParams()[1];
        int seconds = settings.getIntParams()[2];
        this.timeAmount = (minutes * 60) + seconds;

        this.setDelay(settings.getDelay());
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (this.itemIds.isEmpty()) return false;

        for (int id : this.itemIds) {
            HabboItem item = room.getHabboItem(id);
            if (item != null) {
                // AQUI VAI A LÓGICA DO SEU CONTADOR
                // Exemplo: se for um marcador de Banzai
                // item.setTimer(this.mode, this.timeAmount);
            }
        }
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.itemIds, this.mode, this.timeAmount, this.getDelay()
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.itemIds = data.itemIds;
            this.mode = data.mode;
            this.timeAmount = data.timeAmount;
            this.setDelay(data.delay);
        }
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    static class JsonData {
        THashSet<Integer> itemIds;
        int mode;
        int timeAmount;
        int delay;

        public JsonData(THashSet<Integer> itemIds, int mode, int timeAmount, int delay) {
            this.itemIds = itemIds;
            this.mode = mode;
            this.timeAmount = timeAmount;
            this.delay = delay;
        }
    }
}