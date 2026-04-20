package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionTeamScoreValue extends InteractionWiredCondition {
    // Certifica-te que este tipo existe no teu WiredConditionType, ou usa CUSTOM
    public static final WiredConditionType type = WiredConditionType.CUSTOM;

    private GameTeamColors teamColor = GameTeamColors.RED;
    private int requiredScore = 1;

    public WiredConditionTeamScoreValue(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionTeamScoreValue(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        // No teu emulador, tentamos pegar o jogo ativo no quarto.
        // Se room.getGame() pedir a classe, usamos a classe base Game.class que me enviaste.
        com.eu.habbo.habbohotel.games.Game game = room.getGame(com.eu.habbo.habbohotel.games.Game.class);

        if (game != null) {
            // Buscamos o objeto da equipe baseado na cor selecionada no Wired
            com.eu.habbo.habbohotel.games.GameTeam team = game.getTeam(this.teamColor);

            if (team != null) {
                // Verificamos se a pontuação total da equipe atingiu o objetivo
                return team.getTotalScore() >= this.requiredScore;
            }
        }

        return false;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(
                this.teamColor,
                this.requiredScore
        ));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        try {
            String wiredData = set.getString("wired_data");
            if (wiredData != null && wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.teamColor = data.teamColor;
                this.requiredScore = data.requiredScore;
            }
        } catch (Exception e) {
            this.teamColor = GameTeamColors.RED;
            this.requiredScore = 1;
        }
    }

    @Override
    public void onPickUp() {
        this.teamColor = GameTeamColors.RED;
        this.requiredScore = 1;
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(5); // Limite visual
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(2); // Indica que vamos enviar 2 parâmetros inteiros
        message.appendInt(this.teamColor.type); // Primeiro parâmetro (Cor)
        message.appendInt(this.requiredScore);  // Segundo parâmetro (Pontos)
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings) {
        // Precisamos de pelo menos 2 parâmetros (Cor e Pontos)
        if (settings.getIntParams().length < 2) return false;

        this.teamColor = GameTeamColors.values()[settings.getIntParams()[0]];
        this.requiredScore = settings.getIntParams()[1];

        return true;
    }

    static class JsonData {
        GameTeamColors teamColor;
        int requiredScore;

        public JsonData(GameTeamColors teamColor, int requiredScore) {
            this.teamColor = teamColor;
            this.requiredScore = requiredScore;
        }
    }
}