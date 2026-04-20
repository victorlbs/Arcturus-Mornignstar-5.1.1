package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WiredEffectMandaSinal extends InteractionWiredEffect {
    public static final WiredEffectType type;
    private int delay = 0;

    public WiredEffectMandaSinal(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        System.out.println("WiredEffectMandaSinal carregado com ResultSet e Item.");
    }

    public WiredEffectMandaSinal(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        System.out.println("WiredEffectMandaSinal criado com ID: " + id);
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("Serializando dados do wired.");
        message.appendBoolean(false);
        message.appendInt(5);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(1);
        message.appendInt(this.getDelay());
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(this.getDelay());
    }

    public boolean saveData(WiredSettings settings, GameClient gameClient) {
        this.setDelay(settings.getDelay());
        System.out.println("Dados do wired salvos com delay: " + this.getDelay());
        return true;
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (room == null) {
            System.out.println("Execução falhou: sala nula.");
            return false;
        }
        System.out.println("Executando ação do wired.");
        room.getFloorItems().forEach((item) -> {
            if ("sinal".equals(item.getBaseItem().getInteractionType().getName())) {
                item.setExtradata("1");
                item.needsUpdate(true);
                room.updateItemState(item);
                System.out.println("Item 'sinal' atualizado: " + item.getId());
            }
        });
        return true;
    }

    public String getWiredData() {
        System.out.println("Obtendo dados do wired.");
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.delay));
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        System.out.println("Carregando dados do wired: " + wiredData);
        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.delay = data.delay;
        } else {
            try {
                if (!wiredData.isEmpty()) {
                    this.delay = Integer.parseInt(wiredData);
                }
            } catch (Exception ignored) {
                System.out.println("Erro ao converter delay: " + ignored.getMessage());
            }
        }
        this.setDelay(this.delay);
    }

    public void onPickUp() {
        System.out.println("Resetando wired ao ser pego.");
        this.delay = 0;
        this.setDelay(0);
    }

    public WiredEffectType getType() {
        return type;
    }

    static {
        type = WiredEffectType.RESET_TIMERS;
        System.out.println("Tipo do wired inicializado como RESET_TIMERS.");
    }

    static class JsonData {
        int delay;

        public JsonData(int delay) {
            this.delay = delay;
            System.out.println("JsonData criado com delay: " + delay);
        }
    }
}
