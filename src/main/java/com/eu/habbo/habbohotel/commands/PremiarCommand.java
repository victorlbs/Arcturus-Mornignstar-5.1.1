package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboInfo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.generic.alerts.StaffAlertWithLinkComposer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PremiarCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(PremiarCommand.class);

    public PremiarCommand() {
        super("cmd_badge", Emulator.getTexts().getValue("commands.keys.premiar").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length < 2) {
            gameClient.getHabbo().whisper("Uso: :cmd_badge <usuário>", RoomChatMessageBubbles.ALERT);
            return true;
        }

        String username = params[1];
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(username);

        if (habbo == null) {
            HabboInfo habboInfo = HabboManager.getOfflineHabboInfo(username);
            if (habboInfo == null) {
                gameClient.getHabbo().whisper("Usuário não encontrado!", RoomChatMessageBubbles.ALERT);
                return true;
            }
            return giveBadge(gameClient, habboInfo.getId(), username, null);
        }

        return giveBadge(gameClient, habbo.getHabboInfo().getId(), username, habbo);
    }

    private boolean giveBadge(GameClient gameClient, int userId, String username, Habbo habbo) {
        String currentBadge = null;

        try (Connection connection = Emulator.getDatabase().getDataSource().getConnection();
             PreparedStatement selectStatement = connection.prepareStatement("SELECT premio FROM users WHERE id = ?")) {

            selectStatement.setInt(1, userId);
            try (ResultSet resultSet = selectStatement.executeQuery()) {
                if (resultSet.next()) {
                    currentBadge = resultSet.getString("premio");
                }
            }

            String nextBadge = getNextBadge(currentBadge);

            try (PreparedStatement updateStatement = connection.prepareStatement("UPDATE users SET premio = ? WHERE id = ?")) {
                updateStatement.setString(1, nextBadge);
                updateStatement.setInt(2, userId);
                updateStatement.executeUpdate();
            }

            try (PreparedStatement insertStatement = connection.prepareStatement(
                    "INSERT INTO users_badges (user_id, badge_code, slot_id) VALUES (?, ?, 0)")) {
                insertStatement.setInt(1, userId);
                insertStatement.setString(2, nextBadge);
                insertStatement.execute();
            }

            // Se o usuário estiver online, adiciona o emblema no perfil instantaneamente


            habbo.addBadge(nextBadge);

            if (habbo != null && habbo.getRoomUnit() != null) {
                habbo.getHabboInfo().getCurrentRoom().kickHabbo(habbo, true);
            }


            int amount;

            try {
                amount = Integer.valueOf(5);
            } catch (Exception e) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_points.invalid_amount"), RoomChatMessageBubbles.ALERT);
                return true;
            }



            if (amount != 0) {
                habbo.givePoints(5, amount);

                if (habbo.getHabboInfo().getCurrentRoom() != null)
                    habbo.whisper(Emulator.getTexts().getValue("commands.generic.cmd_points.received").replace("%amount%", amount + "").replace("%type%", Emulator.getTexts().getValue("seasonal.name." + 5)), RoomChatMessageBubbles.ALERT);
                else
                    habbo.alert(Emulator.getTexts().getValue("commands.generic.cmd_points.received").replace("%amount%", amount + "").replace("%type%", Emulator.getTexts().getValue("seasonal.name." + 5)));

                // habbo.getClient().sendResponse(new UserPointsComposer(habbo.getHabboInfo().getCurrencyAmount(type), amount, type));

                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_points.send").replace("%amount%", amount + "").replace("%user%", username).replace("%type%", Emulator.getTexts().getValue("seasonal.name." + 5)), RoomChatMessageBubbles.ALERT);

            }



            gameClient.getHabbo().whisper(username + " recebeu o emblema: " + nextBadge, RoomChatMessageBubbles.ALERT);
            return true;

        } catch (SQLException e) {
            LOGGER.error("Erro ao dar emblema para o usuário: " + username, e);
            gameClient.getHabbo().whisper("Erro ao conceder emblema.", RoomChatMessageBubbles.ALERT);
            return false;
        }
    }

    private String getNextBadge(String currentBadge) {
        if (currentBadge == null || currentBadge.isEmpty()) {
            return "NV1";
        }

        if (currentBadge.matches("NV\\d+")) {
            int currentLevel = Integer.parseInt(currentBadge.substring(2));
            return "NV" + (currentLevel + 1);
        }

        return "NV1";  // Caso o valor na coluna `premio` não seja válido
    }
}
