package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import java.util.Collections;
import java.util.concurrent.TimeUnit;


public class FumarCommand extends Command {
    public FumarCommand() {
        super(null, new String[]{"fumar", "fuma", "fume", "fumo"});
    }
   @Override
    public boolean handle(GameClient gameClient, String[] params) {

        Emulator.getRuntime().gc();


       Habbo habbo = (params.length == 3 && gameClient.getHabbo().hasPermission(Permission.ACC_EMPTY_OTHERS)) ? Emulator.getGameEnvironment().getHabboManager().getHabbo(params[2]) : gameClient.getHabbo();

       gameClient.getHabbo().talk("*Puxando baseado*", RoomChatMessageBubbles.GREEN);
       gameClient.getHabbo().talk("*Puxando esqueiro*", RoomChatMessageBubbles.GREEN);
       gameClient.getHabbo().talk("*Acendendo balao *", RoomChatMessageBubbles.GREEN);
       //gameClient.sendResponse(new MessagesForYouComposer(Collections.singletonList(credits)));
        return true;
    }
}
