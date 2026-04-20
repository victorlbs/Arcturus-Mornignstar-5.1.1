package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.guides.GuideSessionPartnerIsPlayingComposer;

public class ClickThroughCommand extends Command {

    // Chave para salvar no cache se o usuário está com o modo ativado ou não
    public static final String CLICK_THROUGH_KEY = "click_through_enabled";

    public ClickThroughCommand() {
        // Se preferir usar o banco de dados para os nomes do comando:
        // super("cmd_ct", Emulator.getTexts().getValue("commands.keys.cmd_ct").split(";"));

        // Ou direto no código (responde a :ct e :clickthrough):
        super("cmd_commands", new String[]{"ct", "clickthrough"});
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        Habbo habbo = gameClient.getHabbo();

        if (habbo == null) return false;

        // Verifica se o usuário JÁ ESTÁ com o Click Through ativado
        if (habbo.getHabboStats().cache.containsKey(CLICK_THROUGH_KEY)) {
            // DESATIVA
            habbo.getHabboStats().cache.remove(CLICK_THROUGH_KEY);
            habbo.getClient().sendResponse(new GuideSessionPartnerIsPlayingComposer(false));
            habbo.whisper("Modo Click-Through desativado. Cliques normais restaurados.", RoomChatMessageBubbles.ALERT);
        } else {
            // ATIVA
            habbo.getHabboStats().cache.put(CLICK_THROUGH_KEY, true);
            habbo.getClient().sendResponse(new GuideSessionPartnerIsPlayingComposer(true));
            habbo.whisper("Modo Click-Through ativado! Você vai ignorar cliques em mobis e usuários.", RoomChatMessageBubbles.ALERT);
        }

        return true;
    }
}