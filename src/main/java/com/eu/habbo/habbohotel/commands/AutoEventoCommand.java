package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;

public class AutoEventoCommand extends Command {

    public AutoEventoCommand() {
        // "cmd_autoevento" deve ser registrado na DB. Rank 1 já que é para usuários.
        super("cmd_autoevento", new String[]{"autoevento", "irauto"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        // Usaremos uma variável temporária no HabboStats para não precisar mexer na DB
        // Se quiser que salve ao deslogar, seria necessário criar uma coluna na DB.
        boolean isActive = client.getHabbo().getHabboStats().cache.containsKey("auto_evento_enabled");

        if (!isActive) {
            client.getHabbo().getHabboStats().cache.put("auto_evento_enabled", true);
            client.getHabbo().whisper("Você ATIVOU o teletransporte automático para eventos!");
        } else {
            client.getHabbo().getHabboStats().cache.remove("auto_evento_enabled");
            client.getHabbo().whisper("Você DESATIVOU o teletransporte automático para eventos.");
        }

        return true;
    }
}