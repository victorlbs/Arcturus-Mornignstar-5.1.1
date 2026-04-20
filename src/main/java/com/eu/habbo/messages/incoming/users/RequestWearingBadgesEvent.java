package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.inventory.BadgesComponent;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;
import com.eu.habbo.messages.outgoing.users.UserBadgesComposer;

public class RequestWearingBadgesEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        int userId = this.packet.readInt();
        Habbo habbo = Emulator.getGameServer().getGameClientManager().getHabbo(userId);

        if (habbo == null || habbo.getHabboInfo() == null || habbo.getInventory() == null || habbo.getInventory().getBadgesComponent() == null) {
            this.client.sendResponse(new UserBadgesComposer(BadgesComponent.getBadgesOfflineHabbo(userId), userId));
        } else {
            // 1. Envia os emblemas normalmente para o jogo não bugar
            this.client.sendResponse(new UserBadgesComposer(habbo.getInventory().getBadgesComponent().getWearingBadges(), habbo.getHabboInfo().getId()));

            // --- 2. O NOSSO GATILHO WIRED COMEÇA AQUI ---
            // Verifica se quem clicou está num quarto para poder ativar os Wireds
            if (this.client.getHabbo() != null && this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
                System.out.println("[DEBUG] Disparando Wired de Clique no Usuário: " + habbo.getHabboInfo().getUsername());

                WiredHandler.handle(
                        WiredTriggerType.USER_CLICKS_USER, // O Tipo do nosso gatilho
                        this.client.getHabbo().getRoomUnit(), // Quem clicou
                        this.client.getHabbo().getHabboInfo().getCurrentRoom(), // O quarto onde estão
                        new Object[]{ habbo } // Passamos o alvo clicado como informação extra
                );
            }
            // --------------------------------------------
        }
    }
}