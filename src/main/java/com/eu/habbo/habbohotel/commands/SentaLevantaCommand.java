package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;

public class SentaLevantaCommand extends Command {
    public SentaLevantaCommand() {
        super(null, Emulator.getTexts().getValue("commands.keys.cmd_sentalevanta").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (gameClient.getHabbo().getHabboInfo().getRiding() == null) //TODO Make this an event plugin which fires that can be cancelled
            gameClient.getHabbo().getHabboInfo().getCurrentRoom().makeSit(gameClient.getHabbo());
        Thread.sleep(5000); // Espera 5 segundos

        // Faz o avatar se levantar
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().makeStand(gameClient.getHabbo());
        Thread.sleep(5000); // Espera 5 segundos antes de repetir o loop
        return true;
    }
}
