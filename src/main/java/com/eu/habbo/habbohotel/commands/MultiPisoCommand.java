package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;

public class MultiPisoCommand extends Command {

    public MultiPisoCommand() {
        super("cmd_commands", new String[]{"multipiso", "mpiso", "fillmode"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        if (params.length < 3) {
            client.getHabbo().getHabboStats().cache.remove("multipiso_x");
            client.getHabbo().getHabboStats().cache.remove("multipiso_y");
            client.getHabbo().whisper("Modo Multi-Piso desativado.");
            return true;
        }

        try {
            int x = Integer.parseInt(params[1]);
            int y = Integer.parseInt(params[2]);

            if (x * y > 25) { // Limite de segurança para não travar o cliente
                client.getHabbo().whisper("Área muito grande! Use no máximo 5x5 (25 mobis).");
                return true;
            }

            client.getHabbo().getHabboStats().cache.put("multipiso_x", x);
            client.getHabbo().getHabboStats().cache.put("multipiso_y", y);
            client.getHabbo().whisper("Modo Multi-Piso ativado: " + x + "x" + y);
        } catch (NumberFormatException e) {
            client.getHabbo().whisper("Use: :multipiso [largura] [comprimento]");
        }
        return true;
    }
}