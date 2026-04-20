package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectWhisper;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserRotationWiredEffect extends WiredEffectWhisper {
    public UserRotationWiredEffect(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public UserRotationWiredEffect(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        try {
            int rotation = Integer.valueOf(this.message);
            if (roomUnit == null) {
                return false;
            }

            Habbo habbo = room.getHabbo(roomUnit);
            if (habbo != null) {
                roomUnit.setBodyRotation(RoomUserRotation.fromValue(rotation));
                roomUnit.setHeadRotation(RoomUserRotation.fromValue(rotation));
                room.updateHabbo(habbo);
            }
        } catch (Exception var6) {
        }

        return false;
    }
}