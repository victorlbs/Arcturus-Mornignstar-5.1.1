package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemUpdateComposer;
import gnu.trove.set.hash.THashSet;

public class StackAllCommand extends Command {

    public StackAllCommand() {
        // Nome do comando: :stackall
        super("cmd_commands", new String[]{"stackall", "empilhartudo"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) return false;

        // 1. Onde vamos empilhar? (Nas coordenadas atuais do utilizador)
        short targetX = client.getHabbo().getRoomUnit().getX();
        short targetY = client.getHabbo().getRoomUnit().getY();

        // 2. Pegar todos os itens de chão do quarto
        THashSet<HabboItem> items = room.getFloorItems();

        double currentZ = 0.0;

        for (HabboItem item : items) {
            // 3. Atualizar coordenadas X e Y
            item.setX(targetX);
            item.setY(targetY);

            // 4. Definir a altura Z acumulada
            item.setZ(currentZ);

            // 5. Somar a altura deste item para o próximo ficar em cima
            // Usamos a altura base do mobi
            currentZ += item.getBaseItem().getHeight();

            // 6. Notificar o banco de dados e os utilizadores
            item.needsUpdate(true);
            room.sendComposer(new FloorItemUpdateComposer(item).compose());
        }

        client.getHabbo().whisper("Todos os mobis foram empilhados em (" + targetX + ", " + targetY + ")!");
        return true;
    }
}