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

public class WiredEffectSendSignal extends InteractionWiredEffect {
    // Adicione SEND_SIGNAL no seu enum WiredEffectType (ou use o código correto da sua SWF)
    public static final WiredEffectType type = WiredEffectType.SEND_SIGNAL;

    private final THashSet<HabboItem> items;
    private boolean signalFurni = false;
    private boolean signalUser = false;

    public WiredEffectSendSignal(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.items = new THashSet<>();
    }

    public WiredEffectSendSignal(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.items = new THashSet<>();
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Serializando Efeito: Mandar Sinal");

        // Limpa mobis que já não estão no quarto
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
        message.appendString("");

        // Enviamos 2 parâmetros para as Checkboxes (0 = desmarcado, 1 = marcado)
        message.appendInt(2);
        message.appendInt(this.signalFurni ? 1 : 0);
        message.appendInt(this.signalUser ? 1 : 0);

        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("============== DEBUG SAVE MANDAR SINAL ==============");
        this.items.clear();

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
        if (room == null) return false;

        // 1. Salva os Mobis selecionados
        if (settings.getFurniIds() != null) {
            for (int itemId : settings.getFurniIds()) {
                HabboItem item = room.getHabboItem(itemId);
                if (item != null) {
                    this.items.add(item);
                }
            }
        }

        // 2. Salva as Checkboxes
        if (settings.getIntParams().length >= 2) {
            this.signalFurni = settings.getIntParams()[0] == 1;
            this.signalUser = settings.getIntParams()[1] == 1;
        }

        this.setDelay(settings.getDelay());

        System.out.println("[RESULTADO] Mobis salvos: " + this.items.size());
        System.out.println("[RESULTADO] Sinal Mobi? " + this.signalFurni);
        System.out.println("[RESULTADO] Sinal Usuário? " + this.signalUser);
        System.out.println("=====================================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE MANDAR SINAL ==============");
        if (this.items.isEmpty() || room == null) return false;

        System.out.println("[INFO] Enviando sinal para " + this.items.size() + " mobis...");

        for (HabboItem item : this.items) {
            if (room.getHabboItem(item.getId()) != null) {
                // A MÁGICA ACONTECE AQUI:
                // Você avisa o Emulador que um sinal chegou no mobi selecionado.
                // Isso deve ativar o Gatilho "Receber Sinal" que estiver em cima dele!

                // NOTA: Se você já tiver criado o Gatilho Receber Sinal, coloque o tipo correto aqui:
                // WiredHandler.handle(WiredTriggerType.RECEIVE_SIGNAL, (this.signalUser ? roomUnit : null), room, new Object[]{ item });

                System.out.println("[SINAL ENVIADO] Alvo: " + item.getId());
            }
        }

        System.out.println("========================================================");
        return true;
    }

    @Override
    public String getWiredData() {
        List<Integer> itemIds = new ArrayList<>();
        for (HabboItem item : this.items) {
            itemIds.add(item.getId());
        }
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.signalFurni, this.signalUser, this.getDelay(), itemIds));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();
        String wiredData = set.getString("wired_data");

        if (wiredData == null || wiredData.isEmpty()) return;

        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.signalFurni = data.signalFurni;
                this.signalUser = data.signalUser;
                this.setDelay(data.delay);

                for (int id : data.itemIds) {
                    HabboItem item = room.getHabboItem(id);
                    if (item != null) this.items.add(item);
                }
            }
        } catch (Exception e) {
            System.out.println("[!] Erro ao carregar dados do Wired Mandar Sinal.");
        }
    }

    @Override
    public void onPickUp() {
        this.items.clear();
        this.signalFurni = false;
        this.signalUser = false;
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    static class JsonData {
        boolean signalFurni;
        boolean signalUser;
        int delay;
        List<Integer> itemIds;

        public JsonData(boolean signalFurni, boolean signalUser, int delay, List<Integer> itemIds) {
            this.signalFurni = signalFurni;
            this.signalUser = signalUser;
            this.delay = delay;
            this.itemIds = itemIds;
        }
    }
}