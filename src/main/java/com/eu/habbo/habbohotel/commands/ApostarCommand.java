package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import gnu.trove.map.hash.THashMap;

import java.util.Random;

public class ApostarCommand extends Command {
    public ApostarCommand() {
        super("cmd_commands", new String[]{"apostar", "bet"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        if (params.length < 2) {
            client.getHabbo().whisper("Use: :apostar <valor>");
            return true;
        }

        int valor;
        try {
            valor = Integer.parseInt(params[1]);
        } catch (NumberFormatException e) {
            client.getHabbo().whisper("Por favor, insira um número válido.");
            return true;
        }

        if (valor <= 0 || valor > 5000) {
            client.getHabbo().whisper("Você só pode apostar entre 1 e 5000 moedas.");
            return true;
        }

        Habbo habbo = client.getHabbo();

        if (habbo.getHabboInfo().getCredits() < valor) {
            client.getHabbo().whisper("Você não tem moedas suficientes para essa aposta!");
            return true;
        }

        Random random = new Random();
        int resultadoHabbo = random.nextInt(6) + 1; // Dado 1 a 6
        int resultadoBot = random.nextInt(6) + 1;

        String mensagem;
        if (resultadoHabbo > resultadoBot) {
            habbo.giveCredits(valor);
            mensagem = "<b>Você Venceu!</b><br><br>Você tirou 🎲 " + resultadoHabbo + " e eu tirei 🎲 " + resultadoBot + ".<br>Você ganhou <b>" + valor + " moedas!</b>";
        } else if (resultadoHabbo < resultadoBot) {
            habbo.giveCredits(-valor);
            mensagem = "<b>Você Perdeu!</b><br><br>Você tirou 🎲 " + resultadoHabbo + " e eu tirei 🎲 " + resultadoBot + ".<br>Você perdeu <b>" + valor + " moedas!</b>";
        } else {
            mensagem = "<b>Empate!</b><br><br>Ambos tiramos 🎲 " + resultadoHabbo + ". Suas moedas estão seguras.";
        }

        // Alerta visual bonitinho
        THashMap<String, String> keys = new THashMap<>();
        keys.put("title", "Cassino");
        keys.put("message", mensagem);
        habbo.getClient().sendResponse(new BubbleAlertComposer("hotel.event", keys));

        return true;
    }
}