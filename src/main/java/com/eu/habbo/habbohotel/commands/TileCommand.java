package com.eu.habbo.habbohotel.commands;// Ajuste para o package do seu emulador

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.gameclients.GameClient;

public class TileCommand extends Command {

    public TileCommand() {
        // Permissão necessária e o nome do comando
        super("cmd_commands", new String[]{"tile"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        if (params.length < 2) {
            client.getHabbo().whisper("Uso correto: :tile <copy|place|pick|move> ou :tile off");
            return true;
        }

        String action = params[1].toLowerCase();

        // Desativar o comando
        if (action.equals("off")) {
            client.getHabbo().getHabboStats().cache.remove("tile_mode");
            client.getHabbo().whisper("Modo :tile desativado.");
            return true;
        }

        // Validar ações permitidas
        if (!action.equals("copy") && !action.equals("place") && !action.equals("pick") && !action.equals("move")) {
            client.getHabbo().whisper("Ação inválida! Use: copy, place, pick, move ou off.");
            return true;
        }

        // Salva a ação na sessão do usuário para ser usada no próximo clique
        client.getHabbo().getHabboStats().cache.put("tile_mode", action);
        client.getHabbo().whisper("Modo :tile ativado para '" + action + "'. Clique em um quadrado para aplicar.");

        return true;
    }
}