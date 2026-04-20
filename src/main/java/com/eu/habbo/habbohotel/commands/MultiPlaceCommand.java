package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;

public class MultiPlaceCommand extends Command {

    public MultiPlaceCommand() {
        // Define o nome do comando e os atalhos (aliases)
        super("cmd_commands", new String[]{"multi", "multiplace", "mobiX"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        // 1. Se o usuário digitar apenas o comando sem número, desativamos o modo
        if (params.length < 2) {
            client.getHabbo().getHabboStats().cache.remove("multiplace_amount");
            client.getHabbo().whisper("Modo Multiplace desativado.");
            return true;
        }

        try {
            // 2. Tenta converter o parâmetro em número
            int quantidade = Integer.parseInt(params[1]);

            // 3. Trava de segurança para não crashar o cliente/servidor
            if (quantidade < 1 || quantidade > 20) {
                client.getHabbo().whisper("Por favor, escolha uma quantidade entre 1 e 20.");
                return true;
            }

            // 4. Salva a quantidade no cache do Habbo (persiste durante a sessão)
            client.getHabbo().getHabboStats().cache.put("multiplace_amount", quantidade);

            client.getHabbo().whisper("Multiplicador ativado! Agora, ao colocar um mobi, " + quantidade + " serão criados.");

        } catch (NumberFormatException e) {
            // Caso o usuário digite letras em vez de números
            client.getHabbo().whisper("Uso correto: :multi [quantidade]");
        }

        return true;
    }
}