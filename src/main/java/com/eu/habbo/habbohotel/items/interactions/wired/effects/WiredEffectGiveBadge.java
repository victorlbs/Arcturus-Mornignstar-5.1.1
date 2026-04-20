package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.Achievement;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredTrigger;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.*;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.rooms.ForwardToRoomComposer;
import com.eu.habbo.messages.outgoing.rooms.HideDoorbellComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserWhisperComposer;
import com.eu.habbo.messages.outgoing.wired.WiredRewardAlertComposer;
import gnu.trove.map.hash.THashMap;
import gnu.trove.procedure.TObjectProcedure;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WiredEffectGiveBadge extends InteractionWiredEffect {
    public static final WiredEffectType type = WiredEffectType.SHOW_MESSAGE;

    protected String message = "";

    public WiredEffectGiveBadge(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        System.out.println("WiredEffectGiveBadge initialized with ResultSet and Item.");
    }

    public WiredEffectGiveBadge(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        System.out.println("WiredEffectGiveBadge initialized with parameters.");
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("Serializing wired data.");
        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString(this.message);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(type.code);
        message.appendInt(this.getDelay());

        if (this.requiresTriggeringUser()) {
            List<Integer> invalidTriggers = new ArrayList<>();
            System.out.println("Checking for invalid triggers.");
            room.getRoomSpecialTypes().getTriggers(this.getX(), this.getY()).forEach(new TObjectProcedure<InteractionWiredTrigger>() {
                @Override
                public boolean execute(InteractionWiredTrigger object) {
                    if (!object.isTriggeredByRoomUnit()) {
                        invalidTriggers.add(object.getBaseItem().getSpriteId());
                    }
                    return true;
                }
            });
            System.out.println("Invalid triggers found: " + invalidTriggers.size());
            message.appendInt(invalidTriggers.size());
            for (Integer i : invalidTriggers) {
                message.appendInt(i);
            }
        } else {
            message.appendInt(0);
        }
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("Saving wired data.");
        String message = settings.getStringParam();

        if(gameClient.getHabbo() == null || !gameClient.getHabbo().hasPermission(Permission.ACC_SUPERWIRED)) {
            message = Emulator.getGameEnvironment().getWordFilter().filter(message, null);
            message = message.substring(0, Math.min(message.length(), Emulator.getConfig().getInt("hotel.wired.message.max_length", 100)));
        }

        int delay = settings.getDelay();
        System.out.println("Delay set to: " + delay);

        if(delay > Emulator.getConfig().getInt("hotel.wired.max_delay", 20))
            throw new WiredSaveException("Delay too long");

        this.message = message;
        this.setDelay(delay);
        System.out.println("Wired data saved with message: " + this.message);
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        if (this.message.length() > 0) {
            if (roomUnit != null) {
                Habbo habbo = room.getHabbo(roomUnit);

                if (habbo != null) {
                    // Obter o prêmio atual do usuário
                    String premioAtual = getUserPremio(habbo.getHabboInfo().getId());
                    String proximoEmblema = getNextBadge(premioAtual); // Método para determinar o próximo emblema

                    // Atribui o próximo emblema ao usuário
                    if (!proximoEmblema.isEmpty()) {
                        BadgesComponent.createBadge(proximoEmblema, habbo); // Atribui o emblema
                        habbo.getClient().sendResponse(new WiredRewardAlertComposer(WiredRewardAlertComposer.REWARD_RECEIVED_BADGE)); // Envia alerta de recompensa
                        System.out.println("Emblema " + proximoEmblema + " atribuído ao usuário " + habbo.getHabboInfo().getUsername());

                        // Atualiza o valor do NV no banco de dados
                        updateUserNV(habbo.getHabboInfo().getId(), premioAtual);
                    }

                    // Cria a mensagem substituindo os placeholders
                    String msg = this.message.replace("%user%", habbo.getHabboInfo().getUsername())
                            .replace("%online_count%", Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "")
                            .replace("%room_count%", Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size() + "")
                            .replace("%premio%", proximoEmblema); // Atualiza para mostrar o novo emblema

                    // Envia a mensagem ao usuário
                    habbo.getClient().sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(msg, habbo, habbo, RoomChatMessageBubbles.WIRED)));
                    habbo.addBadge(proximoEmblema);

                    habbo.givePoints(5, 5);
                    habbo.giveCredits(100);
                    habbo.whisper("Evento finalizado.");

                    for (Habbo onlineHabbo : Emulator.getGameEnvironment().getHabboManager().getOnlineHabbos().values()) {
                        if (onlineHabbo != null && onlineHabbo.getClient() != null) {
                            THashMap<String, String> keys = new THashMap<>();
                            keys.put("display", "BUBBLE");
                            keys.put("message", "O " + habbo.getHabboInfo().getUsername() + " ganhou o evento!");

                            // Substituir %figure% pela aparência (look) do avatar de cada jogador
                            keys.put("image", Emulator.getConfig().getValue("bubblefriend.image").replace("%figure%", habbo.getHabboInfo().getLook()));

                            // Enviar a resposta para o cliente do jogador online
                            onlineHabbo.getClient().sendResponse(new BubbleAlertComposer("bubblelogin", keys));
                        }
                    }



                    Achievement achievement = Emulator.getGameEnvironment().getAchievementManager().getAchievement("TimeSpentRoomTypeGames");

                    if (achievement == null) {
                        // nada
                    } else {
                        AchievementManager.progressAchievement(habbo.getHabboInfo().getId(), achievement);
                    }













                    // habbo.getClient().sendResponse(new BubbleAlertComposer("bubbleall", "O ganhou " + habbo.getHabboInfo().getUsername() + " o evento."));





                    //kickAllHabbos(room); ELE NAO KIKA MAIS PORQUE O MATA EVENTO FAZ ISSO.
                    // Chama o manipulador de eventos
                    Emulator.getThreading().run(() -> WiredHandler.handle(WiredTriggerType.SAY_SOMETHING, roomUnit, room, new Object[]{msg}));






                    // Verifica se o usuário está ocioso e desocupa se necessário
                    if (habbo.getRoomUnit().isIdle()) {
                        habbo.getRoomUnit().getRoom().unIdle(habbo);
                    }
                    return true;
                }
            } else {
                // Se roomUnit for nulo, envia a mensagem para todos os usuários no quarto
                for (Habbo h : room.getHabbos()) {
                    // Obter o prêmio atual do usuário
                    String premioAtual = getUserPremio(h.getHabboInfo().getId());
                    String proximoEmblema = getNextBadge(premioAtual);

                    if (!proximoEmblema.isEmpty()) {
                        BadgesComponent.createBadge(proximoEmblema, h); // Atribui o emblema
                        h.getClient().sendResponse(new WiredRewardAlertComposer(WiredRewardAlertComposer.REWARD_RECEIVED_BADGE)); // Envia alerta de recompensa
                        System.out.println("Emblema " + proximoEmblema + " atribuído ao usuário " + h.getHabboInfo().getUsername());

                        // Atualiza o valor do NV no banco de dados
                        updateUserNV(h.getHabboInfo().getId(), premioAtual);
                    }

                    h.getClient().sendResponse(new RoomUserWhisperComposer(new RoomChatMessage(this.message.replace("%user%", h.getHabboInfo().getUsername())
                            .replace("%online_count%", Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "")
                            .replace("%room_count%", Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size() + "")
                            .replace("%premio%", proximoEmblema), h, h, RoomChatMessageBubbles.WIRED)));
                }

                return true;
            }
        }
        return false;
    }

    private void kickAllHabbos(Room room) {
        for (Habbo habbo : room.getHabbos()) {
            room.kickHabbo(habbo, true); // O segundo parâmetro define se a expulsão é forçada
            System.out.println("Usuário " + habbo.getHabboInfo().getUsername() + " foi expulso do quarto.");
        }
    }

    // Método para atualizar o valor do NV do usuário no banco de dados
    private void updateUserNV(int userId, String premioAtual) {
        try {
            // Conecta ao banco de dados
            try (java.sql.Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                 java.sql.PreparedStatement statement = connection.prepareStatement("UPDATE users SET premio = ? WHERE id = ?")) {
                // Incrementa o valor do NV
                int novoNV = Integer.parseInt(premioAtual.substring(2)) + 1; // Incrementa o nível
                statement.setString(1, "NV" + novoNV); // Define o novo valor
                statement.setInt(2, userId); // Define o ID do usuário
                statement.executeUpdate(); // Executa a atualização
                System.out.println("NV do usuário " + userId + " atualizado para NV" + novoNV);
            }
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine("Erro ao atualizar o NV do usuário: " + e.getMessage());
        }
    }


    // Método para inserir o emblema no banco de dados
    private void insertBadgeIntoDatabase(int userId, String badgeCode) {
        try (java.sql.Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             java.sql.PreparedStatement statement = connection.prepareStatement("INSERT INTO users_badges (`id`, `user_id`, `slot_id`, `badge_code`) VALUES (null, ?, 0, ?)")) {
            statement.setInt(1, userId);
            statement.setString(2, badgeCode);
            statement.executeUpdate();
            System.out.println("Emblema " + badgeCode + " inserido no banco de dados para o usuário ID: " + userId);
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine("Erro ao inserir emblema no banco de dados: " + e.getMessage());
        }
    }



    // Método para obter o próximo emblema com base no prêmio atual
    private String getNextBadge(String premio) {
        if (premio.startsWith("NV") && premio.length() > 2) {
            try {
                // Extrai o número do prêmio atual
                int currentLevel = Integer.parseInt(premio.substring(2));
                int nextLevel = currentLevel + 1; // Incrementa o nível
                return "NV" + nextLevel; // Retorna o próximo emblema
            } catch (NumberFormatException e) {
                System.out.println("Erro ao converter o prêmio para nível: " + e.getMessage());
                return ""; // Retorna vazio se houver um erro
            }
        }
        return ""; // Retorna vazio se o prêmio não estiver no formato esperado
    }



    // Método para obter o prêmio do usuário
    private String getUserPremio(int userId) {
        String premio = "";
        try {
            // Conecta ao banco de dados
            try (java.sql.Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                 java.sql.PreparedStatement statement = connection.prepareStatement("SELECT premio FROM users WHERE id = ?")) {
                statement.setInt(1, userId);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        premio = resultSet.getString("premio"); // Obtém o valor da coluna 'premio'
                        System.out.println("User premio retrieved: " + premio);
                    } else {
                        System.out.println("No premio found for userId: " + userId);
                    }
                }
            }
        } catch (SQLException e) {
            Emulator.getLogging().logErrorLine("Erro ao buscar o prêmio do usuário: " + e.getMessage());
        }
        return premio;
    }


    @Override
    public String getWiredData() {
        System.out.println("Getting wired data.");
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.message, this.getDelay()));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        System.out.println("Loading wired data: " + wiredData);

        if(wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            this.setDelay(data.delay);
            this.message = data.message;
            System.out.println("Wired data loaded: message = " + this.message + ", delay = " + this.getDelay());
        } else {
            this.message = "";

            if (wiredData.split("\t").length >= 2) {
                super.setDelay(Integer.valueOf(wiredData.split("\t")[0]));
                this.message = wiredData.split("\t")[1];
            }

            this.needsUpdate(true);
            System.out.println("Wired data split: delay = " + this.getDelay() + ", message = " + this.message);
        }
    }

    @Override
    public void onPickUp() {
        System.out.println("Picking up wired effect.");
        this.message = "";
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        System.out.println("Getting wired effect type.");
        return type;
    }

    @Override
    public boolean requiresTriggeringUser() {
        System.out.println("Checking if requires triggering user.");
        return true;
    }

    static class JsonData {
        String message;
        int delay;

        public JsonData(String message, int delay) {
            this.message = message;
            this.delay = delay;
        }
    }
}
