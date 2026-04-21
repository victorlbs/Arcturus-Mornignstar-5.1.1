package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;

public class MultiPlaceCommand extends Command {

    public MultiPlaceCommand() {
        super("cmd_commands", new String[]{"multi", "multiplace", "mobiX"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        Room room = client.getHabbo().getHabboInfo().getCurrentRoom();

        // 1. Verifica se o usuário está em um quarto
        if (room == null) return false;

        // 2. VALIDAÇÃO DE SEGURANÇA
        // Garante que apenas o dono ou staffs com permissão possam ativar o multiplicador
        if (room.getOwnerId() != client.getHabbo().getHabboInfo().getId() && !client.getHabbo().hasPermission("acc_anyroomowner")) {
            client.getHabbo().whisper("Você precisa ser o proprietário do quarto para usar o multiplicador de mobis!");
            return true;
        }

        // 3. Se o usuário digitar apenas o comando sem número, desativamos o modo
        if (params.length < 2) {
            client.getHabbo().getHabboStats().cache.remove("multiplace_amount");
            client.getHabbo().whisper("Modo Multiplace desativado.");
            return true;
        }

        try {
            int quantidade = Integer.parseInt(params[1]);

            // 4. Trava de segurança (limite de 20 mobis por vez)
            if (quantidade < 1 || quantidade > 20) {
                client.getHabbo().whisper("Por favor, escolha uma quantidade entre 1 e 20.");
                return true;
            }

            // 5. Salva a quantidade no cache do Habbo (persiste durante a sessão)
            client.getHabbo().getHabboStats().cache.put("multiplace_amount", quantidade);

            client.getHabbo().whisper("Multiplicador ativado! Agora, ao colocar um mobi, " + quantidade + " unidades serão colocadas.");

        } catch (NumberFormatException e) {
            client.getHabbo().whisper("Uso correto: :multi [quantidade]");
        }

        return true;
    }
}