package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemUpdateComposer;
import gnu.trove.set.hash.THashSet;

public class GirarIguaisCommand extends Command {

    public GirarIguaisCommand() {
        super("cmd_commands", new String[]{"girariguais", "giguais", "rotateall"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) return false;

        if (params.length < 2) {
            client.getHabbo().whisper("Uso: :giguais [0-7] ou :giguais proximo");
            return true;
        }

        // 1. Identificar o mobi modelo (o que você está pisando)
        HabboItem mobiModelo = room.getTopItemAt(
                client.getHabbo().getRoomUnit().getX(),
                client.getHabbo().getRoomUnit().getY()
        );

        if (mobiModelo == null) {
            client.getHabbo().whisper("Fique em cima do mobi que deseja usar como modelo!");
            return true;
        }

        int novaRotacao;
        String acao = params[1].toLowerCase();

        try {
            // 2. Lógica de Rotação Inteligente
            if (acao.equals("proximo") || acao.equals("next")) {
                // Soma +2 (90 graus) e usa o módulo % 8 para voltar ao 0 após o 7
                novaRotacao = (mobiModelo.getRotation() + 2) % 8;
            } else {
                novaRotacao = Integer.parseInt(acao);
            }

            if (novaRotacao < 0 || novaRotacao > 7) {
                client.getHabbo().whisper("A rotação deve ser entre 0 e 7.");
                return true;
            }

            // 3. Aplicar a todos os itens iguais no quarto
            int itemBaseId = mobiModelo.getBaseItem().getId();
            THashSet<HabboItem> items = room.getFloorItems();
            int contador = 0;

            for (HabboItem item : items) {
                if (item.getBaseItem().getId() == itemBaseId) {
                    item.setRotation(novaRotacao);
                    item.needsUpdate(true);
                    room.sendComposer(new FloorItemUpdateComposer(item).compose());
                    contador++;
                }
            }

            client.getHabbo().whisper("Sucesso! " + contador + " mobis girados para a posição " + novaRotacao);

        } catch (NumberFormatException e) {
            client.getHabbo().whisper("Use um número de 0-7 ou o botão de Giro +90°.");
        }

        return true;
    }
}