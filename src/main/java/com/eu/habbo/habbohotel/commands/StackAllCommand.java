package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemUpdateComposer;
import gnu.trove.set.hash.THashSet;

public class StackAllCommand extends Command {

    public StackAllCommand() {
        super("cmd_commands", new String[]{"stackall", "empilhartudo"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();

        // 1. Verifica se o usuário está em um quarto
        if (room == null) return false;

        // 2. Validação: O usuário é o dono do quarto ou tem permissão de staff?
        // Se quiser que apenas o dono use:
        if (room.getOwnerId() != client.getHabbo().getHabboInfo().getId()) {
            client.getHabbo().whisper("Apenas o proprietário do quarto pode usar este comando!");
            return true;
        }

        short targetX = client.getHabbo().getRoomUnit().getX();
        short targetY = client.getHabbo().getRoomUnit().getY();
        THashSet<HabboItem> items = room.getFloorItems();

        double currentZ = 0.0;

        for (HabboItem item : items) {
            item.setX(targetX);
            item.setY(targetY);
            item.setZ(currentZ);

            // Incrementa a altura baseado no mobi atual
            currentZ += item.getBaseItem().getHeight();

            item.needsUpdate(true);
            room.sendComposer(new FloorItemUpdateComposer(item).compose());
        }

        client.getHabbo().whisper("Todos os mobis foram empilhados em (" + targetX + ", " + targetY + ")!");
        return true;
    }
}