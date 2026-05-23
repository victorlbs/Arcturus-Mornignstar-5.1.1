package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionSkateBoardJump extends InteractionRoller {

    public InteractionSkateBoardJump(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public InteractionSkateBoardJump(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        // Ativa o efeito de skate (ID 71)
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo != null) {
            room.giveEffect(roomUnit, 71, -1);

            // Adiciona progresso na conquista SkateBoardJump
            Achievement skateJump = Emulator.getGameEnvironment().getAchievementManager().getAchievement("SkateBoardJump");
            if (skateJump != null) {
                AchievementManager.progressAchievement(habbo.getHabboInfo().getId(), skateJump);
            }
        }
    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);

        // Remove o efeito ao sair do mobi
        if (roomUnit.getEffectId() == 71) {
            room.giveEffect(roomUnit, 0, -1);
        }
    }

    @Override
    public void onPickUp(Room room) {
        // Remove o efeito de todos que estiverem em cima do mobi ao pegá-lo
        for (Habbo habbo : room.getHabbosOnItem(this)) {
            if (habbo.getRoomUnit().getEffectId() == 71) {
                room.giveEffect(habbo.getRoomUnit(), 0, -1);
            }
        }
    }
}