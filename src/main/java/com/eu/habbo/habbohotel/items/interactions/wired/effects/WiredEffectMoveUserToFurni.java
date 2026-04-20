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
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WiredEffectMoveUserToFurni extends InteractionWiredEffect {
    // Usamos um tipo padrão que permite selecionar mobis na interface
    public static final WiredEffectType type = WiredEffectType.TELEPORT;
    private final List<HabboItem> items = new ArrayList<>();

    public WiredEffectMoveUserToFurni(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectMoveUserToFurni(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        // Regra de Ouro: Se não houver nenhum utilizador (ex: foi ativado por um Timer), não faz nada!
        if (roomUnit == null) return false;

        // Limpa mobis que já não estão no quarto
        this.items.removeIf(item -> room.getHabboItem(item.getId()) == null);

        if (this.items.isEmpty()) return false;

        // A Mágica: Escolhe um dos mobis selecionados à sorte
        HabboItem targetItem = this.items.get(Emulator.getRandom().nextInt(this.items.size()));

        if (targetItem != null) {
            // Usa a função nativa e segura do teu Room.java para teleportar o utilizador para cima do mobi
            room.teleportRoomUnitToItem(roomUnit, targetItem);
            return true;
        }

        return false;
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
        if (room == null) return false;

        this.items.clear();

        // Permite selecionar até 5 mobis de destino (podes mudar este número se quiseres)
        if (settings.getFurniIds().length > 5) {
            throw new WiredSaveException("Você pode selecionar no máximo 5 mobis.");
        }

        for (int i = 0; i < settings.getFurniIds().length; i++) {
            HabboItem item = room.getHabboItem(settings.getFurniIds()[i]);
            if (item != null) {
                this.items.add(item);
            }
        }

        this.setDelay(settings.getDelay());
        return true;
    }

    @Override
    public String getWiredData() {
        this.items.removeIf(item -> item.getRoomId() != this.getRoomId() || Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(item.getId()) == null);

        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.getDelay(),
                this.items.stream().map(HabboItem::getId).collect(Collectors.toList())
        ));
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        this.items.removeIf(item -> item.getRoomId() != this.getRoomId() || room.getHabboItem(item.getId()) == null);

        message.appendBoolean(false);
        message.appendInt(5); // Máximo de seleção na interface do jogo: 5
        message.appendInt(this.items.size());

        for (HabboItem item : this.items) {
            message.appendInt(item.getId());
        }

        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();
        String wiredData = set.getString("wired_data");

        if (wiredData != null && wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.setDelay(data.delay);

            for (Integer id: data.itemIds) {
                HabboItem item = room.getHabboItem(id);
                if (item != null) {
                    this.items.add(item);
                }
            }
        }
    }

    @Override
    public void onPickUp() {
        this.setDelay(0);
        this.items.clear();
    }

    @Override
    protected long requiredCooldown() {
        return 495;
    }

    // Estrutura para salvar no banco de dados JSON
    static class JsonData {
        int delay;
        List<Integer> itemIds;

        public JsonData(int delay, List<Integer> itemIds) {
            this.delay = delay;
            this.itemIds = itemIds;
        }
    }
}