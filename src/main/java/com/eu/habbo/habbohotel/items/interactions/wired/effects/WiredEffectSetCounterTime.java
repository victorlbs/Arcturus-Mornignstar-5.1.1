package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.games.InteractionGameTimer;
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
import java.util.List;
import java.util.stream.Collectors;

public class WiredEffectSetCounterTime extends InteractionWiredEffect {
    // Defina o tipo de efeito. Se não existir no seu enum, use o ID correspondente.
    public static final WiredEffectType type = WiredEffectType.CUSTOM;

    private final THashSet<HabboItem> items = new THashSet<>();
    private int mode = 0;       // 0: Aumentar, 1: Diminuir, 2: Configurar
    private int timeAmount = 0; // Valor total em segundos (Minutos * 60 + Segundos)

    public WiredEffectSetCounterTime(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectSetCounterTime(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        // Limpa itens que não existem mais no quarto antes de enviar
        this.items.removeIf(item -> item.getRoomId() != this.getRoomId() || room.getHabboItem(item.getId()) == null);

        message.appendBoolean(false);
        message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
        message.appendInt(this.items.size());
        for (HabboItem item : this.items) {
            message.appendInt(item.getId());
        }
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString(""); // Sem caixa de texto

        // Sincronização com os Sliders da UI
        message.appendInt(2);               // Quantidade de parâmetros inteiros
        message.appendInt(this.mode);       // Parâmetro 1: Seleção de modo
        message.appendInt(this.timeAmount); // Parâmetro 2: Valor do tempo

        message.appendInt(0);               // Quantidade de parâmetros de texto
        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        if (settings.getIntParams().length < 2) {
            throw new WiredSaveException("Dados insuficientes para salvar o cronômetro.");
        }

        this.mode = settings.getIntParams()[0];
        this.timeAmount = settings.getIntParams()[1];

        this.items.clear();
        Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (room != null) {
            for (int id : settings.getFurniIds()) {
                HabboItem item = room.getHabboItem(id);
                if (item instanceof InteractionGameTimer) {
                    this.items.add(item);
                } else {
                    // Feedback visual via sussurro se houver mobi inválido
                    gameClient.getHabbo().whisper("Atenção: Apenas cronômetros podem ser selecionados!");
                    return false;
                }
            }
        }

        this.setDelay(settings.getDelay());
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (this.items.isEmpty()) return false;

        for (HabboItem item : this.items) {
            if (item instanceof InteractionGameTimer) {
                InteractionGameTimer timer = (InteractionGameTimer) item;

                int currentTime = timer.getTimeNow();
                int newTime;

                switch (this.mode) {
                    case 0: // Aumentar
                        newTime = currentTime + this.timeAmount;
                        break;
                    case 1: // Diminuir
                        newTime = Math.max(0, currentTime - this.timeAmount);
                        break;
                    case 2: // Configurar Valor Fixo
                    default:
                        newTime = this.timeAmount;
                        break;
                }

                timer.setTimeNow(newTime);
                room.updateItem(timer); // Notifica o quarto para atualizar o visor do mobi
            }
        }

        return true;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.mode,
                this.timeAmount,
                this.getDelay(),
                this.items.stream().map(HabboItem::getId).collect(Collectors.toList())
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();
        String wiredData = set.getString("wired_data");

        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.mode = data.mode;
            this.timeAmount = data.time;
            this.setDelay(data.delay);

            for (Integer id : data.itemIds) {
                HabboItem item = room.getHabboItem(id);
                if (item != null) this.items.add(item);
            }
        }
    }

    @Override
    public void onPickUp() {
        this.items.clear();
        this.mode = 0;
        this.timeAmount = 0;
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    static class JsonData {
        int mode;
        int time;
        int delay;
        List<Integer> itemIds;

        public JsonData(int mode, int time, int delay, List<Integer> itemIds) {
            this.mode = mode;
            this.time = time;
            this.delay = delay;
            this.itemIds = itemIds;
        }
    }
}