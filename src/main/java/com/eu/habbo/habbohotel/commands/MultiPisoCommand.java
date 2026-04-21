package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;

public class MultiPisoCommand extends Command {

    public MultiPisoCommand() {
        super("cmd_commands", new String[]{"multipiso", "mpiso", "fillmode"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();

        // 1. Verifica se o usuário está em um quarto
        if (room == null) return false;

        // 2. VALIDAÇÃO DE SEGURANÇA
        // Apenas o dono ou staffs podem ativar o modo de preenchimento múltiplo
        if (room.getOwnerId() != client.getHabbo().getHabboInfo().getId() && !client.getHabbo().hasPermission("acc_anyroomowner")) {
            client.getHabbo().whisper("Você precisa ser o proprietário do quarto para ativar o Multi-Piso!");
            return true;
        }

        // Se o comando for digitado sem argumentos (ou menos de 3), desativa o modo
        if (params.length < 3) {
            client.getHabbo().getHabboStats().cache.remove("multipiso_x");
            client.getHabbo().getHabboStats().cache.remove("multipiso_y");
            client.getHabbo().whisper("Modo Multi-Piso desativado.");
            return true;
        }

        try {
            int x = Integer.parseInt(params[1]);
            int y = Integer.parseInt(params[2]);

            // Limite de segurança rigoroso (25 mobis por clique)
            if (x * y > 25) {
                client.getHabbo().whisper("Área muito grande! Para evitar lag, use no máximo 5x5 (25 mobis).");
                return true;
            }

            // Salva as dimensões no cache temporário do HabboStats
            client.getHabbo().getHabboStats().cache.put("multipiso_x", x);
            client.getHabbo().getHabboStats().cache.put("multipiso_y", y);
            client.getHabbo().whisper("Modo Multi-Piso ativado: " + x + "x" + y);
            client.getHabbo().whisper("Dica: Clique em um mobi no chão para replicá-lo nesta área.");

        } catch (NumberFormatException e) {
            client.getHabbo().whisper("Use: :multipiso [largura] [comprimento]");
        }
        return true;
    }
}