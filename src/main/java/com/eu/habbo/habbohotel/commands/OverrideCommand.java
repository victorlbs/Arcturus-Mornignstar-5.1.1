package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

public class OverrideCommand extends Command {
    public OverrideCommand() {
        super("cmd_update_items", new String[]{"override", "atravessar"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) return false;

        RoomUnit unit = client.getHabbo().getRoomUnit();

        // Verificamos se ele já tem tiles na lista de override (cache local do RoomUnit)
        // Se a lista não estiver vazia, ele quer desativar.
        if (unit.canOverrideTile(unit.getCurrentLocation())) {
            unit.clearOverrideTiles(); // Limpa a lista (método na linha 631 do seu RoomUnit)
            client.getHabbo().whisper("Modo Override: DESATIVADO.");
        } else {
            // Ativa o override para TODOS os tiles do mapa
            for (short x = 0; x < room.getLayout().getMapSizeX(); x++) {
                for (short y = 0; y < room.getLayout().getMapSizeY(); y++) {
                    RoomTile tile = room.getLayout().getTile(x, y);
                    if (tile != null) {
                        unit.addOverrideTile(tile); // Adiciona (método na linha 616 do seu RoomUnit)
                    }
                }
            }
            client.getHabbo().whisper("Modo Override: ATIVADO. Você agora caminha sobre tudo!");
        }

        // Atualiza o status para o servidor processar a mudança de caminhada
        unit.statusUpdate(true);
        room.sendComposer(new RoomUserStatusComposer(unit).compose());

        return true;
    }
}