package com.eu.habbo.habbohotel.items.interactions.wired.effects;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.items.interactions.wired.effects.WiredEffectWhisper;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.RoomDataComposer;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectForwardUserToRoom extends WiredEffectWhisper {
    public WiredEffectForwardUserToRoom(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectForwardUserToRoom(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        String message = settings.getStringParam();

        int roomId;
        try {
            roomId = Integer.parseInt(message);
        } catch (NumberFormatException var9) {
            gameClient.getHabbo().whisper("Invalido. Bote o  ID.");
            throw new WiredSaveException("");
        }

        Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(roomId);
        if (room == null) {
            gameClient.getHabbo().whisper("Quarto informado nao existe.");
            throw new WiredSaveException("");
        } else {
            Habbo user = gameClient.getHabbo();
            int currentRoomId = user.getHabboInfo().getCurrentRoom().getId();
            if (!user.hasPermission(Permission.ACC_ANYROOMOWNER) && room.getOwnerId() != user.getHabboInfo().getId()) {
                gameClient.getHabbo().whisper("A entrada do ID da sala deve pertencer a uma sala de sua propriedade.");
                throw new WiredSaveException("");
            } else if (roomId == currentRoomId) {
                gameClient.getHabbo().whisper("O ID da sala especificado é o mesmo da sua sala atual.");
                throw new WiredSaveException("");
            } else {
                if (gameClient.getHabbo() == null || !gameClient.getHabbo().hasPermission(Permission.ACC_SUPERWIRED)) {
                    message = Emulator.getGameEnvironment().getWordFilter().filter(message, (Habbo)null);
                    message = message.substring(0, Math.min(message.length(), Emulator.getConfig().getInt("hotel.wired.message.max_length", 100)));
                }

                int delay = settings.getDelay();
                if (delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20)) {
                    throw new WiredSaveException("Delay too long");
                } else {
                    this.message = message;
                    this.setDelay(delay);
                    return true;
                }
            }
        }
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        Habbo habbo = room.getHabbo(roomUnit);
        if (habbo == null) {
            return false;
        } else {
            this.roomForwardPlayer(habbo, room);
            return true;
        }
    }

    private void roomForwardPlayer(Habbo actor, Room room) {
        int targetRoomID = 0;

        try {
            targetRoomID = Integer.parseInt(this.message);
        } catch (Exception var6) {
        }

        if (targetRoomID > 0) {
            Room targetRoom = Emulator.getGameEnvironment().getRoomManager().loadRoom(targetRoomID);
            if (targetRoom != null) {
                Room currentRoom = actor.getHabboInfo().getCurrentRoom();
                if (currentRoom != null) {
                    Emulator.getGameEnvironment().getRoomManager().logExit(actor);
                    currentRoom.removeHabbo(actor, true);
                    actor.getHabboInfo().setCurrentRoom((Room)null);
                }

                if (actor.getRoomUnit() != null && actor.getRoomUnit().isTeleporting) {
                    actor.getRoomUnit().isTeleporting = false;
                }

                actor.getClient().sendResponse(new RoomDataComposer(targetRoom, actor, true, true));
                Emulator.getGameEnvironment().getRoomManager().enterRoom(actor, targetRoom.getId(), "", true);
            }
        }
    }
}