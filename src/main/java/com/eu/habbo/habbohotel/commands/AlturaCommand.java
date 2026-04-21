package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserStatusComposer;

public class AlturaCommand extends Command {

    public AlturaCommand() {
        super("cmd_commands", new String[]{"altura", "height", "z"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();


        if (room == null) {
            return false;
        }

        // --- VALIDAÇÃO DE PROPRIETÁRIO ---
        // Apenas o dono do quarto ou staffs podem mudar a própria altura
        if (room.getOwnerId() != client.getHabbo().getHabboInfo().getId() && !client.getHabbo().hasPermission("acc_anyroomowner")) {
            client.getHabbo().whisper("Você precisa ser o proprietário do quarto para ajustar sua altura!");
            return true;
        }
        // ---------------------------------

        RoomUnit roomUnit = client.getHabbo().getRoomUnit();

        // 2. Ajuda técnica
        if (params.length < 2) {
            client.getHabbo().whisper("Uso: :altura [valor] (Ex: :altura 1.5)");
            return true;
        }

        try {
            // 3. Captura e trata o valor (aceita ponto ou vírgula)
            double novaAltura = Double.parseDouble(params[1].replace(",", "."));

            // Limite de segurança opcional: evitar que o boneco suma no teto ou chão
            if (novaAltura < -10 || novaAltura > 50) {
                client.getHabbo().whisper("Valor de altura inválido (tente entre -10 e 50).");
                return true;
            }

            // 4. Aplicar a altura na RoomUnit
            roomUnit.setZ(novaAltura);

            // 5. Atualizar o status visual para todos no quarto
            roomUnit.statusUpdate(true);
            room.sendComposer(new RoomUserStatusComposer(roomUnit).compose());

            client.getHabbo().whisper("Altura definida para: " + novaAltura);

        } catch (NumberFormatException e) {
            client.getHabbo().whisper("Por favor, utiliza apenas números (Ex: 0.8)");
        }

        return true;
    }
}