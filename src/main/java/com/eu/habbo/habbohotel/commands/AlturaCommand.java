package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

public class AlturaCommand extends Command {

    public AlturaCommand() {
        // Define o nome do comando na DB e os aliases (atalhos)
        super("cmd_commands", new String[]{"altura", "height", "z"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        // 1. Verificação de segurança: O utilizador está num quarto?
        if (client.getHabbo().getHabboInfo().getCurrentRoom() == null) {
            return false;
        }

        RoomUnit roomUnit = client.getHabbo().getRoomUnit();

        // 2. Se o utilizador digitar apenas :altura, resetamos ou mostramos ajuda
        if (params.length < 2) {
            client.getHabbo().whisper("Uso: :altura [valor] (Ex: :altura 1.5 ou :altura 0.1)");
            return true;
        }

        try {
            // 3. Captura o valor digitado
            // Substituímos vírgula por ponto para evitar erros de digitação (ex: 0,1 vira 0.1)
            double novaAltura = Double.parseDouble(params[1].replace(",", "."));

            // 4. Aplicar a altura na RoomUnit
            // Usamos o método setZ que já existe no teu ficheiro RoomUnit.java
            roomUnit.setZ(novaAltura);

            // 5. IMPORTANTE: Atualizar o status visual
            // Sem isso, o boneco muda a altura no código, mas os outros jogadores não vêem
            roomUnit.statusUpdate(true);

            // Força o envio do pacote de status para todos no quarto
            client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserStatusComposer(roomUnit).compose());

            client.getHabbo().whisper("Altura definida para: " + novaAltura);

        } catch (NumberFormatException e) {
            client.getHabbo().whisper("Por favor, utiliza apenas números (Ex: 0.5)");
        }

        return true;
    }
}