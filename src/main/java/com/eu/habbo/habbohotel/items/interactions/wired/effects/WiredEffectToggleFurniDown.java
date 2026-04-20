package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionCrackable;
import com.eu.habbo.habbohotel.items.interactions.InteractionDefault;
import com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeBlock;
import com.eu.habbo.habbohotel.items.interactions.games.freeze.InteractionFreezeTile;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectToggleFurni;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class WiredEffectToggleFurniDown extends InteractionWiredEffect {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredEffectToggleFurni.class);
    public static final WiredEffectType type;
    private final THashSet<HabboItem> items = new THashSet();

    public WiredEffectToggleFurniDown(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectToggleFurniDown(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        THashSet<HabboItem> items = new THashSet();
        TObjectHashIterator var4 = this.items.iterator();

        while(true) {
            HabboItem item;
            do {
                if (!var4.hasNext()) {
                    var4 = items.iterator();

                    while(var4.hasNext()) {
                        item = (HabboItem)var4.next();
                        this.items.remove(item);
                    }

                    message.appendBoolean(false);
                    message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
                    message.appendInt(this.items.size());
                    var4 = this.items.iterator();

                    while(var4.hasNext()) {
                        item = (HabboItem)var4.next();
                        message.appendInt(item.getId());
                    }

                    message.appendInt(this.getBaseItem().getSpriteId());
                    message.appendInt(this.getId());
                    message.appendString("");
                    message.appendInt(0);
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
                        Iterator var8 = invalidTriggers.iterator();

                        while(var8.hasNext()) {
                            Integer i = (Integer)var8.next();
                            message.appendInt(i);
                        }
                    } else {
                        message.appendInt(0);
                    }

                    return;
                }

                item = (HabboItem)var4.next();
            } while(item.getRoomId() == this.getRoomId() && Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(item.getId()) != null);

            items.add(item);
        }
    }

    public boolean saveData(ClientMessage packet, GameClient gameClient) {
        packet.readInt();
        packet.readString();
        int count = packet.readInt();
        if (count > Emulator.getConfig().getInt("hotel.wired.furni.selection.count")) {
            return false;
        } else {
            this.items.clear();

            for(int i = 0; i < count; ++i) {
                HabboItem item = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(packet.readInt());
                if (!(item instanceof InteractionFreezeBlock) && !(item instanceof InteractionFreezeTile) && !(item instanceof InteractionCrackable)) {
                    this.items.add(item);
                }
            }

            this.setDelay(packet.readInt());
            return true;
        }
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        THashSet<HabboItem> itemsToRemove = new THashSet();
        TObjectHashIterator var5 = this.items.iterator();

        while(true) {
            while(var5.hasNext()) {
                HabboItem item = (HabboItem)var5.next();
                if (item != null && item.getRoomId() != 0 && (item instanceof InteractionDefault || item instanceof InteractionMultiHeight)) {
                    try {
                        if (item.getBaseItem().getStateCount() > 1) {
                            int state = 0;
                            if (!item.getExtradata().isEmpty()) {
                                try {
                                    state = Integer.valueOf(item.getExtradata());
                                } catch (NumberFormatException var9) {
                                }
                            }

                            int newState = state - 1;
                            if (state == 0) {
                                newState = item.getBaseItem().getStateCount() - 1;
                            }

                            item.setExtradata(newState + "");
                            item.needsUpdate(true);
                            room.updateItem(item);
                        }
                    } catch (Exception var10) {
                        LOGGER.error("Caught exception", var10);
                    }
                } else {
                    itemsToRemove.add(item);
                }
            }

            this.items.removeAll(itemsToRemove);
            return true;
        }
    }

    public String getWiredData() {
        StringBuilder wiredData = new StringBuilder(this.getDelay() + "\t");
        if (this.items != null && !this.items.isEmpty()) {
            TObjectHashIterator var2 = this.items.iterator();

            while(var2.hasNext()) {
                HabboItem item = (HabboItem)var2.next();
                wiredData.append(item.getId()).append(";");
            }
        }

        return wiredData.toString();
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();
        String[] wiredData = set.getString("wired_data").split("\t");
        if (wiredData.length >= 1) {
            this.setDelay(Integer.valueOf(wiredData[0]));
        }

        if (wiredData.length == 2 && wiredData[1].contains(";")) {
            String[] var4 = wiredData[1].split(";");
            int var5 = var4.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                String s = var4[var6];
                HabboItem item = room.getHabboItem(Integer.valueOf(s));
                if (!(item instanceof InteractionFreezeBlock) && !(item instanceof InteractionFreezeTile) && !(item instanceof InteractionCrackable) && item != null) {
                    this.items.add(item);
                }
            }
        }

    }

    public void onPickUp() {
        this.items.clear();
        this.setDelay(0);
    }

    public WiredEffectType getType() {
        return type;
    }

    static {
        type = WiredEffectType.TOGGLE_STATE;
    }
}
    