package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.AddFloorItemComposer;

public class FillFloorCommand extends Command {

    public FillFloorCommand() {
        // Uso: :piso [largura] [comprimento]
        super("cmd_commands", new String[]{"piso", "fill", "preencher"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) return false;

        if (params.length < 3) {
            client.getHabbo().whisper("Uso: :piso [largura] [comprimento]");
            return true;
        }

        try {
            int width = Integer.parseInt(params[1]);
            int length = Integer.parseInt(params[2]);

            // Limite de segurança para não crashar o servidor (máximo 10x10 = 100 mobis)
            if (width * length > 100) {
                client.getHabbo().whisper("Área muito grande! O limite é de 100 mobis por vez.");
                return true;
            }

            // Pega o mobi que você está pisando para usar como modelo
            HabboItem modelItem = room.getTopItemAt(
                    client.getHabbo().getRoomUnit().getX(),
                    client.getHabbo().getRoomUnit().getY()
            );

            if (modelItem == null) {
                client.getHabbo().whisper("Fique em cima do mobi que deseja usar como piso!");
                return true;
            }

            short startX = client.getHabbo().getRoomUnit().getX();
            short startY = client.getHabbo().getRoomUnit().getY();

            // Loop Duplo: X e Y
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < length; y++) {
                    // Pula o primeiro quadrado porque você já está pisando no original
                    if (x == 0 && y == 0) continue;

                    short targetX = (short) (startX + x);
                    short targetY = (short) (startY + y);

                    RoomTile tile = room.getLayout().getTile(targetX, targetY);

                    // Verifica se o quadrado existe no quarto
                    if (tile != null) {
                        HabboItem newItem = Emulator.getGameEnvironment().getItemManager().createItem(
                                client.getHabbo().getHabboInfo().getId(),
                                modelItem.getBaseItem(),
                                0, 0, ""
                        );

                        if (newItem != null) {
                            newItem.setX(targetX);
                            newItem.setY(targetY);
                            newItem.setZ(tile.getStackHeight());
                            newItem.setRotation(modelItem.getRotation());
                            newItem.setRoomId(room.getId());

                            room.addHabboItem(newItem);
                            newItem.needsUpdate(true);
                            room.sendComposer(new AddFloorItemComposer(newItem, client.getHabbo().getHabboInfo().getUsername()).compose());
                        }
                    }
                }
            }

            client.getHabbo().whisper("Área preenchida com sucesso!");

        } catch (NumberFormatException e) {
            client.getHabbo().whisper("Insira números válidos para largura e comprimento.");
        }

        return true;
    }
}