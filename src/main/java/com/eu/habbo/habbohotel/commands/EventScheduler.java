package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import gnu.trove.map.hash.THashMap;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class EventScheduler {

    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static boolean eventsStarted = false; // Para verificar se os eventos já foram iniciados

    // Defina os IDs dos quartos onde os eventos serão chamados
    private static final int[] EVENT_ROOM_IDS = {1, 2, 3}; // Substitua pelos IDs reais dos quartos

    public static void startAutoEvent() {
        if (eventsStarted) return; // Impede a reinicialização

        eventsStarted = true;
        System.out.println("Iniciando o agendador de eventos...");
        scheduler.scheduleAtFixedRate(() -> {
            System.out.println("Executando evento automático...");
            for (int roomId : EVENT_ROOM_IDS) {
                triggerEvent(roomId);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    public static void onUserLogin(Habbo habbo) {
        // Inicia os eventos automáticos quando o usuário faz login
        startAutoEvent();
    }

    private static void triggerEvent(int roomId) {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(roomId);
        if (room == null) {
            System.out.println("Erro: Quarto com ID " + roomId + " não encontrado.");
            return;
        }

        int onlineUsers = Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().size();
        System.out.println("Usuários online: " + onlineUsers);

        for (Map.Entry<Integer, Habbo> set : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().entrySet()) {
            Habbo habbo = set.getValue();
            if (habbo.getHabboStats().blockStaffAlerts) {
                System.out.println("Usuário " + habbo.getHabboInfo().getUsername() + " bloqueia alertas.");
                continue;
            }

            THashMap<String, String> codes = new THashMap<>();
            codes.put("ROOMNAME", room.getName());
            codes.put("ROOMID", String.valueOf(roomId));
            codes.put("TIME", Emulator.getDate().toString());
            codes.put("MESSAGE", "<br><b>Opaa! Um evento está acontecendo no quarto " +
                    room.getName() + "!</b><br><br>Participe e ganhe prêmios exclusivos!");

            ServerMessage msg = new BubbleAlertComposer("hotel.event", codes).compose();
            habbo.getClient().sendResponse(msg);

            System.out.println("Enviado alerta para " + habbo.getHabboInfo().getUsername());
        }
    }
}
