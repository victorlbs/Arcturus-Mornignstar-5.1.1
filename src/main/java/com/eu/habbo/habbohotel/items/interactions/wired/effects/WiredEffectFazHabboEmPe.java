package com.eu.habbo.habbohotel.items.interactions.wired.effects;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.guides.GuideSessionPartnerIsPlayingComposer;
import gnu.trove.procedure.TObjectProcedure;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WiredEffectFazHabboEmPe extends InteractionWiredEffect {
    public static final WiredEffectType type;
    private int delay = 0;

    public WiredEffectFazHabboEmPe(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectFazHabboEmPe(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public void serializeWiredData(ServerMessage message, Room room) {
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
        if (this.requiresTriggeringUser()) {
            final List<Integer> invalidTriggers = new ArrayList();
            room.getRoomSpecialTypes().getTriggers(this.getX(), this.getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() {
                public boolean execute(InteractionWiredTrigger object) {
                    if (!object.isTriggeredByRoomUnit()) {
                        invalidTriggers.add(object.getBaseItem().getSpriteId());
                    }

                    return true;
                }
            });
            message.appendInt(invalidTriggers.size());
            Iterator var4 = invalidTriggers.iterator();

            while(var4.hasNext()) {
                Integer i = (Integer)var4.next();
                message.appendInt(i);
            }
        } else {
            message.appendInt(0);
        }

    }

    public boolean saveData(WiredSettings settings, GameClient gameClient) {
        this.setDelay(settings.getDelay());
        return true;
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (roomUnit != null) {
            Habbo habbo = room.getHabbo(roomUnit);
            if (habbo != null && habbo.getHabboInfo().getRiding() == null) {
                habbo.getHabboInfo().getCurrentRoom().makeStand(habbo);


            }
        }

        return false;
    }

    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new WiredEffectUserSit.JsonData(this.delay));
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            WiredEffectUserSit.JsonData data = (WiredEffectUserSit.JsonData)WiredHandler.getGsonBuilder().create().fromJson(wiredData, WiredEffectUserSit.JsonData.class);
            this.delay = data.delay;
        } else {
            try {
                if (!wiredData.equals("")) {
                    this.delay = Integer.parseInt(wiredData);
                }
            } catch (Exception var5) {
            }
        }

        this.setDelay(this.delay);
    }

    public void onPickUp() {
        this.delay = 0;
        this.setDelay(0);
    }

    public WiredEffectType getType() {
        return type;
    }

    static {
        type = WiredEffectType.RESET_TIMERS;
    }

    static class JsonData {
        int delay;

        public JsonData(int delay) {
            this.delay = delay;
        }
    }
}
