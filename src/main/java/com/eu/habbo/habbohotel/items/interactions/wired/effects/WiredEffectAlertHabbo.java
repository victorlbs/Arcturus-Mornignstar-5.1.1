package com.eu.habbo.habbohotel.items.interactions.wired.effects;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectWhisper;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectAlertHabbo extends WiredEffectWhisper {
    public WiredEffectAlertHabbo(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectAlertHabbo(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            habbo.alert(this.message + "  - [AVISO] Esse alerta foi emitido por um wired de efeito.");
            return true;
        } else {
            return false;
        }
    }
}