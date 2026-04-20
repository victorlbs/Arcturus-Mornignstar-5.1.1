package com.eu.habbo.habbohotel.items.interactions.wired.effects;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectWhisper;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTalkComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectAddHighscore extends WiredEffectWhisper {
    public WiredEffectAddHighscore(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectAddHighscore(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null) {
            return false;
        } else {
            habbo.getHabboInfo().getCurrentRoom().sendComposer((new RoomUserTalkComposer(new RoomChatMessage("[Wired] " + this.message, habbo, RoomChatMessageBubbles.NORMAL))).compose());
            Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement("TimeSpentRoomTypeChat");

            if (achievement == null) {
              // nada
            } else {
                AchievementManager.progressAchievement(habbo.getHabboInfo().getId(), achievement);
            }




            return true;
        }
    }
}