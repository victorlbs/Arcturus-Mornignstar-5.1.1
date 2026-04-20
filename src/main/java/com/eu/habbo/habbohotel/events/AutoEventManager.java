package com.eu.habbo.habbohotel.events; // Sugestão de pacote

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsUpdatedComposer;
import gnu.trove.map.hash.THashMap;

import java.util.Map;
import java.util.Random;

public class


AutoEventManager {
    private static final Random random = new Random();
    private static final int[] VALID_ROOM_IDS = {202, 165, 194, 192, 193, 195, 196, 138, 129, 128, 126, 130, 151, 152, 158, 159, 160};

    public static boolean isEventActive = false;

    // Método que inicia o ciclo infinito
    public static void init() {
        // Agenda para rodar a cada 5 minutos (300 segundos)
        Emulator.getThreading().run(AutoEventManager::executeCycle, 500 * 1000);
    }

    private static void executeCycle() {
        if (!isEventActive) {
            startEvent();
        }
        // Re-agenda a si mesmo para o próximo ciclo
        init();
    }

    public static void startEvent() {
        isEventActive = true;
        int eventRoomId = VALID_ROOM_IDS[random.nextInt(VALID_ROOM_IDS.length)];

        // No Arcturus, usamos o loadRoom para garantir que o quarto carregue mesmo vazio
        Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(eventRoomId);

        if (room != null) {
            // 1. Abre o quarto
            room.setState(RoomState.OPEN);
            room.sendComposer(new RoomSettingsUpdatedComposer(room).compose());

            // 2. Notifica os jogadores após 2 segundos
            Emulator.getThreading().run(() -> notifyPlayers(room), 2000);

            // 3. Fecha o quarto após 15 segundos (10s após a notificação + 5s margem)
            Emulator.getThreading().run(() -> closeRoom(room), 15000);
        } else {
            isEventActive = false;
        }
    }

    private static void notifyPlayers(Room room) {
        String message = "<br><b>🚀 Um novo evento começou!</b>" +
                "<br><br>O quarto <b>" + room.getName() + "</b> está aberto por apenas 10 segundos!";

        THashMap<String, String> codes = new THashMap<>();
        codes.put("ROOMNAME", room.getName());
        codes.put("ROOMID", room.getId() + "");
        codes.put("MESSAGE", message);

        ServerMessage msg = new BubbleAlertComposer("hotel.event", codes).compose();

        for (Habbo habbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
            if (!habbo.getHabboStats().blockStaffAlerts) {
                habbo.getClient().sendResponse(msg);
            }
        }
    }

    private static void closeRoom(Room room) {
        room.setState(RoomState.LOCKED);
        room.sendComposer(new RoomSettingsUpdatedComposer(room).compose());
        isEventActive = false;
    }
}