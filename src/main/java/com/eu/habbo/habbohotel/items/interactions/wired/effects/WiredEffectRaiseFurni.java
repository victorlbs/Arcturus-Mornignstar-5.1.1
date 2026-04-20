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
import gnu.trove.set.hash.THashSet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WiredEffectRaiseFurni extends InteractionWiredEffect {
    // Certifique-se de adicionar RAISE_FURNI no seu enum WiredEffectType
    public static final WiredEffectType type = WiredEffectType.RAISE_FURNI;

    private final THashSet<HabboItem> items;
    private double targetHeight = 0.0;

    public WiredEffectRaiseFurni(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.items = new THashSet<>();
    }

    public WiredEffectRaiseFurni(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.items = new THashSet<>();
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Tentando abrir a interface de Levantar Mobi.");

        THashSet<HabboItem> toRemove = new THashSet<>();
        for (HabboItem item : this.items) {
            if (item.getRoomId() != this.getRoomId() || room.getHabboItem(item.getId()) == null) {
                toRemove.add(item);
            }
        }
        this.items.removeAll(toRemove);

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.items.size());

        for (HabboItem item : this.items) {
            message.appendInt(item.getId());
        }

        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString(String.valueOf(this.targetHeight));
        message.appendInt(1);
        message.appendInt((int)(this.targetHeight * 100));
        message.appendInt(0);

        // 2. O SEGREDO DA JANELA:
        // Se 'this.getType().code' não abrir nada, tenta o código 0 (Janela de Alternar Estado)
        // O código 0 é universal e SEMPRE permite clicar nos mobis do quarto.
        message.appendInt(0);

        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("============== DEBUG SAVE ALTURA ==============");
        this.items.clear();

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
        if (room == null) return false;

        // 1. Salva os Mobis selecionados no quarto
        if (settings.getFurniIds() != null) {
            for (int itemId : settings.getFurniIds()) {
                HabboItem item = room.getHabboItem(itemId);
                if (item != null) {
                    this.items.add(item);
                }
            }
        }

        // 2. O HACK DO SLIDER DE ATRASO
        // A SWF envia o atraso em "ticks" (1 tick = 0.5s).
        // Vamos usar esse valor como a nossa Altura Z!
        // Ex: Slider em "1.0 Segundos" envia o valor 2. (2 / 2.0 = Altura 1.0)
        // Ex: Slider em "2.5 Segundos" envia o valor 5. (5 / 2.0 = Altura 2.5)

        this.targetHeight = settings.getDelay() / 2.0;

        // Zera o delay real para o Efeito acontecer na hora, sem esperar!
        this.setDelay(0);

        System.out.println("[RESULTADO] Mobis salvos: " + this.items.size());
        System.out.println("[RESULTADO] Altura definida (Via Slider de Atraso): " + this.targetHeight);
        System.out.println("===============================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE ALTURA ==============");
        if (this.items.isEmpty() || room == null) return false;

        boolean executou = false;

        for (HabboItem item : this.items) {
            // Verifica se o item ainda existe no quarto
            if (room.getHabboItem(item.getId()) != null) {
                System.out.println("[INFO] Alterando mobi " + item.getId() + " para a altura Z: " + this.targetHeight);

                // 1. Muda a altura real do mobi
                item.setZ(this.targetHeight);

                // 2. O seu Room.java faz a mágica (sobe o mobi e atualiza o chão para os Habbos)
                room.updateItem(item);

                executou = true;
            }
        }

        System.out.println("[RESULTADO] Sucesso? " + executou);
        System.out.println("==================================================");
        return executou;
    }

    @Override
    public String getWiredData() {
        List<Integer> itemIds = new ArrayList<>();
        for (HabboItem item : this.items) {
            itemIds.add(item.getId());
        }
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.targetHeight, this.getDelay(), itemIds));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();
        String wiredData = set.getString("wired_data");

        // 1. CORREÇÃO DO ERRO DO PRINT: Se for novo/vazio, não tenta ler nada!
        if (wiredData == null || wiredData.isEmpty()) {
            return;
        }

        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.targetHeight = data.targetHeight;
                this.setDelay(data.delay);

                for (int id : data.itemIds) {
                    HabboItem item = room.getHabboItem(id);
                    if (item != null) this.items.add(item);
                }
            }
        } catch (Exception e) {
            System.out.println("[!] Erro ao carregar dados do Wired Altura: " + e.getMessage());
        }
    }

    @Override
    public void onPickUp() {
        this.items.clear();
        this.targetHeight = 0.0;
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    static class JsonData {
        double targetHeight;
        int delay;
        List<Integer> itemIds;

        public JsonData(double targetHeight, int delay, List<Integer> itemIds) {
            this.targetHeight = targetHeight;
            this.delay = delay;
            this.itemIds = itemIds;
        }
    }
}