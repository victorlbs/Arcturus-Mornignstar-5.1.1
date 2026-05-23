package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.pets.Pet;
import com.eu.habbo.habbohotel.pets.RideablePet;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionHorseJump extends InteractionRoller {

    private static final Logger LOGGER = LoggerFactory.getLogger(InteractionHorseJump.class);

    public InteractionHorseJump(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public InteractionHorseJump(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        // 1. Tenta identificar o Habbo que está nessa posição (X, Y)
        Habbo habbo = room.getHabbo(roomUnit);

        // 2. Se o habbo é nulo, pode ser que o próprio Pet (a montaria) tenha pisado.
        // Se for um Pet, verificamos quem é o dono dele (o cavaleiro)
        if (habbo == null) {
            Pet pet = room.getPet(roomUnit);
            if (pet instanceof RideablePet) {
                Habbo rider = ((RideablePet) pet).getRider();
                if (rider != null) {
                    processarConquista(rider);
                }
            }
        } else {
            // 3. Se um Habbo pisou, verificamos se ele está montado em algo
            if (habbo.getHabboInfo().getRiding() != null) {
                processarConquista(habbo);
            }
        }
    }

    private void processarConquista(Habbo habbo) {
        RideablePet montaria = habbo.getHabboInfo().getRiding();

        // Aqui acessamos o getPetData() diretamente do objeto montaria,
        // já que ele herda de Pet
        if (montaria != null && montaria.getPetData().getType() == 15) {

            Achievement horseJump = Emulator.getGameEnvironment().getAchievementManager().getAchievement("RoomHorseJumpCount");
            if (horseJump != null) {
                AchievementManager.progressAchievement(habbo.getHabboInfo().getId(), horseJump);
                LOGGER.info("Conquista computada para: " + habbo.getHabboInfo().getUsername());
            }
        }
    }
}