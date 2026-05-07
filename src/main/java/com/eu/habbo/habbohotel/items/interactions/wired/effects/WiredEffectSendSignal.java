package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class WiredEffectSendSignal extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.CUSTOM; // Defina um tipo ou use um existente
    private final THashSet<HabboItem> items = new THashSet<>();

    public WiredEffectSendSignal(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectSendSignal(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (this.items.isEmpty()) return false;

        // O efeito percorre todos os mobis que você selecionou (ex: as Antenas)
        for (HabboItem item : this.items) {
            // Ele dispara um sinal de "Mudança de Estado" como se alguém tivesse clicado no mobi
            // Isso vai ativar qualquer Gatilho "Recebe Sinal" que esteja ouvindo este mobi
            WiredHandler.handle(WiredTriggerType.STATE_CHANGED, roomUnit, room, new Object[]{item});
        }

        return true;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        this.items.removeIf(item -> item.getRoomId() != this.getRoomId() || room.getHabboItem(item.getId()) == null);

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.items.size());
        for (HabboItem item : this.items) {
            message.appendInt(item.getId());
        }
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString(""); // Aqui você poderia salvar as opções de checkbox se quisesse
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    // 1. Adicionamos o GameClient no parâmetro para cumprir o contrato da superclasse
    @Override
    public boolean saveData(WiredSettings settings, com.eu.habbo.habbohotel.gameclients.GameClient gameClient) throws com.eu.habbo.messages.incoming.wired.WiredSaveException {
        this.items.clear();
        Room room = com.eu.habbo.Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());

        if (room != null) {
            // Verificação opcional: apenas o dono ou admins podem salvar esse sinal?
            // if (!room.hasRights(gameClient.getHabbo())) return false;

            for (int id : settings.getFurniIds()) {
                HabboItem item = room.getHabboItem(id);
                if (item != null) {
                    this.items.add(item);
                }
            }
        }

        this.setDelay(settings.getDelay());
        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.items.stream().map(HabboItem::getId).collect(Collectors.toList()),
                this.getDelay()
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.setDelay(data.delay);
            for (Integer id : data.itemIds) {
                HabboItem item = room.getHabboItem(id);
                if (item != null) this.items.add(item);
            }
        }
    }

    @Override
    public void onPickUp() { this.items.clear(); }

    @Override
    public WiredEffectType getType() { return type; }

    static class JsonData {
        List<Integer> itemIds;
        int delay;
        public JsonData(List<Integer> itemIds, int delay) { this.itemIds = itemIds; this.delay = delay; }
    }
}