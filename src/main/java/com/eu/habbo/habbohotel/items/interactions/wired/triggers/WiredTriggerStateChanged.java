package com.eu.habbo.habbohotel.items.interactions.wired.triggers;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WiredTriggerStateChanged extends InteractionWiredTrigger {
    // Definimos o tipo como STATE_CHANGED para o emulador saber que interface abrir
    public static final WiredTriggerType type = WiredTriggerType.STATE_CHANGED;

    // Lista de IDs dos mobis que estamos a monitorizar
    private final List<Integer> itemIds = new ArrayList<>();

    // Construtores padrão exigidos pelo emulador
    public WiredTriggerStateChanged(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredTriggerStateChanged(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public WiredTriggerType getType() {
        return type;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        // O emulador Arcturus passa o mobi que mudou de estado no stuff[0]
        if (stuff != null && stuff.length > 0 && stuff[0] instanceof HabboItem) {
            HabboItem itemQueMudou = (HabboItem) stuff[0];

            // A Mágica: Verificamos se o ID do mobi que mudou está na nossa lista
            return this.itemIds.contains(itemQueMudou.getId());
        }
        return false;
    }

    @Override
    public String getWiredData() {
        // Transformamos a lista de IDs numa estrutura JSON para salvar na DB
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.itemIds));
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        // Estrutura padrão para enviar os mobis selecionados para o cliente (jogo)
        message.appendBoolean(false); // Confirmação de seleção
        message.appendInt(15);        // Limite máximo de mobis (5 nos antigos, 15 nos novos)
        message.appendInt(this.itemIds.size()); // Quantidade atual selecionada

        for (int id : this.itemIds) {
            message.appendInt(id); // Lista os IDs
        }

        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString(""); // Sem mensagem de texto neste Wired
        message.appendInt(0); // Sem parâmetros inteiros extras
        message.appendInt(0); // Sem parâmetros inteiros extras
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        this.itemIds.clear();

        // Quando o usuário clica em "Pronto", o emulador nos dá os IDs selecionados
        for (int id : settings.getFurniIds()) {
            this.itemIds.add(id);
        }
        return true;
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.itemIds.clear();
        String wiredData = set.getString("wired_data");

        // Carregamos os dados salvos em formato JSON
        if (wiredData != null && wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            if (data != null && data.itemIds != null) {
                this.itemIds.addAll(data.itemIds);
            }
        }
    }

    @Override
    public void onPickUp() {
        // Limpa a lista quando o Wired é pego do quarto
        this.itemIds.clear();
    }

    // Classe auxiliar para a conversão JSON (padrão que já usas)
    static class JsonData {
        List<Integer> itemIds;
        public JsonData(List<Integer> itemIds) { this.itemIds = itemIds; }
    }
}