package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

public class InfoCommand extends Command {
    public InfoCommand() {
        super("cmd_commands", new String[]{"info"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        // Inverte o estado de um atributo temporário no player
        boolean isDebugging = client.getHabbo().getHabboStats().cache.containsKey("debug_mobi");

        if (!isDebugging) {
            client.getHabbo().getHabboStats().cache.put("debug_mobi", true);
            client.getHabbo().whisper("Modo INFO ativado. Clique em um mobi para ver os detalhes.", RoomChatMessageBubbles.ALERT);
        } else {
            client.getHabbo().getHabboStats().cache.remove("debug_mobi");
            client.getHabbo().whisper("Modo INFO desativado.", RoomChatMessageBubbles.ALERT);
        }

        return true;
    }
}