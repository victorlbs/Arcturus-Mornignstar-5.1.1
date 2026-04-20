package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessage;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnitEffect;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserTalkComposer;

public class WelcomeCommand extends Command
{
    public WelcomeCommand()
    {
        super("cmd_welcome", Emulator.getTexts().getValue("essentials.cmd_welcome.keys").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception
    {
        if(params.length == 2)
        {
            Habbo habbo = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(params[1]);

            if(habbo == null)
            {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pull.not_found").replace("%user%", params[1]), RoomChatMessageBubbles.ALERT);
                return true;
            }
            else if(habbo == gameClient.getHabbo())
            {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pull.pull_self2"), RoomChatMessageBubbles.ALERT);
                return true;
            }
            else
            {
                int distanceX = habbo.getRoomUnit().getX() - gameClient.getHabbo().getRoomUnit().getX();
                int distanceY = habbo.getRoomUnit().getY() - gameClient.getHabbo().getRoomUnit().getY();

                if(distanceX < -2 || distanceX > 2 || distanceY < -2 || distanceY > 2)
                {
                    gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pull.cant_reach").replace("%user%", params[1]), RoomChatMessageBubbles.ALERT);
                    return true;
                }
                else
                {
                    RoomTile tile = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getTileInFront(gameClient.getHabbo().getRoomUnit().getCurrentLocation(), gameClient.getHabbo().getRoomUnit().getBodyRotation().getValue());

                    if (tile != null && tile.isWalkable())
                    {
                        if (gameClient.getHabbo().getHabboInfo().getCurrentRoom().getLayout().getDoorTile() == tile)
                        {
                            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_pull.invalid").replace("%username%", params[1]));
                            return true;
                        }
                        habbo.getRoomUnit().setGoalLocation(tile);
                        for (String s : Emulator.getTexts().getValue("essentials.cmd_welcome.text").split(";"))
                        {
                            gameClient.getHabbo().shout(s
                                    .replace("%username%", habbo.getHabboInfo().getUsername())
                                    .replace("%greeter_username%", gameClient.getHabbo().getHabboInfo().getUsername())
                                    //.replace("%greeter_rank%", Emulator.getGameEnvironment().getPermissionsManager().getRankName(gameClient.getHabbo().getHabboInfo().getRank()))
                                    .replace("%hotelname%", Emulator.getConfig().getValue("hotel.name"))
                                    .replace("%onlinecount%", Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "")
                                    .replace("%hotelplayername%", Emulator.getConfig().getValue("hotel.player.name"))
                            );
                        } }
                }
            }
            return true;
        }

        return true;
    }
}
