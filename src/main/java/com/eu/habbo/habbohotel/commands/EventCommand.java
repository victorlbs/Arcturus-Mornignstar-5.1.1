package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsSavedComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsUpdatedComposer;
import gnu.trove.map.hash.THashMap;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.*;

public class EventCommand extends Command {
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Random random = new Random();
    private static final int[] VALID_ROOM_IDS = {202, 165, 194, 192, 193, 195, 196, 138, 129, 128, 126, 130, 151, 152, 158, 159, 160}; // IDs válidos
    private boolean isEventActive = false;

    public EventCommand() {
        super("cmd_event", Emulator.getTexts().getValue("commands.keys.cmd_event").split(";"));

        // Inicia a execução do evento a cada 5 minutos
        scheduler.scheduleAtFixedRate(() -> {
            if (!isEventActive) {
                startEvent(null);
            }
        }, 0, 5, TimeUnit.MINUTES);
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length > 1 && params[1].equalsIgnoreCase("stop")) {
            stopEvent(gameClient);
            return true;
        }

        if (!isEventActive) {
            isEventActive = true;
            startEvent(gameClient);
        } else {
            gameClient.getHabbo().whisper("⚠️ O evento já está em andamento.");
        }
        return true;
    }

    private void startEvent(GameClient gameClient) {

        int eventRoomId = VALID_ROOM_IDS[random.nextInt(VALID_ROOM_IDS.length)];
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(eventRoomId);
        if (room == null) {
            if (gameClient != null) gameClient.getHabbo().whisper("⚠️ Erro ao acessar o quarto do evento [" + eventRoomId + "].");
            return;
        }

        // Abre o quarto antes de chamar os jogadores
        room.setState(RoomState.OPEN);
        room.setNeedsUpdate(true);
        room.sendComposer(new RoomSettingsUpdatedComposer(room).compose());
        if (gameClient != null) gameClient.sendResponse(new RoomSettingsSavedComposer(room));

        // Aguarda 2 segundos antes de chamar os jogadores
        scheduler.schedule(() -> notifyPlayers(gameClient, room, eventRoomId), 2, TimeUnit.SECONDS);
    }

    private void notifyPlayers(GameClient gameClient, Room room, int eventRoomId) {
        // Notificar todos os usuários
        String message = "<br><b>🚀 Um novo evento começou!</b>" +
                "<br><br>Participe e concorra a prêmios exclusivos!";

        THashMap<String, String> codes = new THashMap<>();
        codes.put("ROOMNAME", room.getName());
        codes.put("ROOMID", eventRoomId + "");
        codes.put("MESSAGE", message);

        ServerMessage msg = new BubbleAlertComposer("hotel.event", codes).compose();
        for (Map.Entry<Integer, Habbo> set : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
            Habbo habbo = set.getValue();
            if (!habbo.getHabboStats().blockStaffAlerts) {
                habbo.getClient().sendResponse(msg);
            }
        }

        // Fechar o quarto após 10 segundos
        scheduler.schedule(() -> closeRoom(gameClient, eventRoomId), 10, TimeUnit.SECONDS);
    }

    private void closeRoom(GameClient gameClient, int eventRoomId) {
        Room eventRoom = Emulator.getGameEnvironment().getRoomManager().getRoom(eventRoomId);
        if (eventRoom != null) {
            eventRoom.setState(RoomState.LOCKED);
            eventRoom.setNeedsUpdate(true);
            eventRoom.sendComposer(new RoomSettingsUpdatedComposer(eventRoom).compose());
            if (gameClient != null) gameClient.sendResponse(new RoomSettingsSavedComposer(eventRoom));
        }
        isEventActive = false;
    }

    private void stopEvent(GameClient gameClient) {
        isEventActive = false;
        gameClient.getHabbo().whisper("✅ O evento foi encerrado com sucesso.");
    }
}
