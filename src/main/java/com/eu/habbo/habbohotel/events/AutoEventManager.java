package com.eu.habbo.habbohotel.events;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomState;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.rooms.RoomSettingsUpdatedComposer;
import gnu.trove.map.hash.THashMap;
import gnu.trove.list.array.TIntArrayList;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Random;

public class AutoEventManager {
    private static final Random random = new Random();
    public static boolean isEventActive = false;

    public static void init() {
        Emulator.getLogging().logStart("[AutoEventManager] Sistema iniciado. Próximo ciclo em 5 minutos.");
        Emulator.getThreading().run(AutoEventManager::executeCycle, 300 * 1000);
    }

    private static void executeCycle() {
        if (!isEventActive) {
            startEvent();
        }
        // Re-agenda o ciclo para manter o sistema rodando infinitamente
        Emulator.getThreading().run(AutoEventManager::executeCycle, 300 * 1000);
    }

    private static TIntArrayList getValidRoomIds() {
        TIntArrayList ids = new TIntArrayList();
        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             Statement statement = connection.createStatement();
             ResultSet set = statement.executeQuery("SELECT room_id FROM eventos_config")) {

            while (set.next()) {
                ids.add(set.getInt("room_id"));
            }
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine("[AutoEventManager] Erro SQL ao carregar quartos: " + e.getMessage());
        }
        return ids;
    }

    public static void startEvent() {
        Emulator.getLogging().logStart("[AutoEventManager] Iniciando tentativa de evento...");
        TIntArrayList validIds = getValidRoomIds();

        if (validIds.isEmpty()) {
            Emulator.getLogging().logStart("[AutoEventManager] Abortado: Nenhum quarto encontrado na tabela eventos_config.");
            return;
        }

        int eventRoomId = validIds.get(random.nextInt(validIds.size()));
        Room room = Emulator.getGameEnvironment().getRoomManager().loadRoom(eventRoomId);

        if (room != null) {
            isEventActive = true;
            room.setState(RoomState.OPEN);
            room.sendComposer(new RoomSettingsUpdatedComposer(room).compose());

            Emulator.getLogging().logStart("[AutoEventManager] Evento iniciado no quarto: [" + room.getName() + "] (ID: " + room.getId() + ")");

            // Inicia as notificações para o hotel
            Emulator.getThreading().run(() -> notifyPlayers(room), 2000);

            // --- INJEÇÃO DO BOT NO QUARTO ---
            Emulator.getThreading().run(() -> spawnEventBot(room), 3500);

            // Fecha o quarto após 15 segundos
            Emulator.getThreading().run(() -> closeRoom(room), 15000);
        } else {
            Emulator.getLogging().logErrorLine("[AutoEventManager] Erro: Não foi possível carregar o quarto ID " + eventRoomId);
            isEventActive = false;
        }
    }

    private static void notifyPlayers(Room room) {
        // Mensagem que aparecerá no alerta visual para todo o hotel
        String message = "<br><b>🚀 Um novo evento começou!</b>" +
                "<br><br>O quarto <b>" + room.getName() + "</b> está aberto!" +
                "<br><br><i>Use :autoevento para ir automaticamente nos próximos!</i>";

        THashMap<String, String> codes = new THashMap<>();
        codes.put("ROOMNAME", room.getName());
        codes.put("ROOMID", room.getId() + "");
        codes.put("MESSAGE", message);

        ServerMessage msg = new BubbleAlertComposer("hotel.event", codes).compose();

        // Percorre todos os jogadores online no hotel
        for (Habbo habbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {

            // --- AÇÃO 1: MOSTRAR PARA TODOS ---
            // Envia o alerta visual para todo mundo que não bloqueou alertas de staff
            if (!habbo.getHabboStats().blockStaffAlerts) {
                habbo.getClient().sendResponse(msg);
            }

            // --- AÇÃO 2: IR AUTOMÁTICO (APENAS COM COMANDO) ---
            // Verifica se este jogador específico ativou o :autoevento
            if (habbo.getHabboStats().cache.containsKey("auto_evento_enabled")) {
                Emulator.getThreading().run(() -> {
                    // Verifica se o jogador ainda está online antes de puxar
                    if (habbo.getClient() != null && habbo.getHabboInfo().getCurrentRoom() != room) {
                        habbo.goToRoom(room.getId());
                    }
                }, 500); // Meio segundo de delay para não bugar com o alerta
            }
        }

        Emulator.getLogging().logStart("[AutoEventManager] Alerta enviado ao hotel. Usuários vips do comando foram puxados.");
    }

    private static void closeRoom(Room room) {
        if (room != null) {
            room.setState(RoomState.LOCKED);
            room.sendComposer(new RoomSettingsUpdatedComposer(room).compose());
            Emulator.getLogging().logStart("[AutoEventManager] Quarto [" + room.getName() + "] fechado com sucesso.");
        }
        isEventActive = false;
    }

    private static void spawnEventBot(Room room) {
        if (room == null) return;

        try {
            // Cria a instância do Frank (Mordomo clássico)
            com.eu.habbo.habbohotel.bots.Bot frank = new com.eu.habbo.habbohotel.bots.Bot(
                    -1,
                    "MOD_Frank",
                    "Organizador de Eventos",
                    "ch-3022-73-73.sh-290-1408.lg-285-73.hd-180-10.hr-893-40.cc-3039-73.ha-3291-73.fa-1206-1408",
                    com.eu.habbo.habbohotel.users.HabboGender.M,
                    0, "Staff"
            );

            frank.setRoom(room);
            frank.setRoomUnit(new com.eu.habbo.habbohotel.rooms.RoomUnit());
            frank.getRoomUnit().setPathFinderRoom(room);

            // Spawn na porta do quarto
            frank.getRoomUnit().setLocation(room.getLayout().getDoorTile());
            frank.getRoomUnit().setInRoom(true);

            room.addBot(frank);
            room.sendComposer(new com.eu.habbo.messages.outgoing.rooms.users.RoomUsersComposer(frank).compose());

            // Sequência de falas do Frank para animar o evento
            frank.talk("🚀 BOA SORTE A TODOS! O EVENTO COMEÇOU!");

            Emulator.getThreading().run(() -> {
                frank.talk("Corram! O quarto fechará em instantes!");
            }, 4000);

            // Remove o bot após 12 segundos (um pouco antes do quarto fechar totalmente)
            Emulator.getThreading().run(() -> {
                room.removeBot(frank);
            }, 12000);

        } catch (Exception e) {
            Emulator.getLogging().logErrorLine("[AutoEventManager] Erro ao spawnar bot: " + e.getMessage());
        }
    }
}