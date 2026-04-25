package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import gnu.trove.map.hash.THashMap;

public class HumorCommand extends Command {
    public HumorCommand() {
        super("cmd_commands", new String[]{"humor", "mood"});
    }

    @Override
    public boolean handle(GameClient client, String[] params) throws Exception {
        if (params.length < 2) {
            client.getHabbo().whisper("Use: :humor <feliz|bravo|triste|festa>");
            return true;
        }

        String tipo = params[1].toLowerCase();
        String mensagem = "";
        String icon = "";

        switch (tipo) {
            case "feliz":
                mensagem = "está se sentindo radiante hoje! ✨";
                icon = "emoji_happy";
                client.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(client.getHabbo(), 168); // Efeito de brilho
                break;
            case "bravo":
                mensagem = "está furioso! Saiam da frente! 💢";
                icon = "emoji_angry";
                client.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(client.getHabbo(), 25); // Efeito de fogo/raiva
                break;
            case "triste":
                mensagem = "está num clima melancólico... 🌧️";
                icon = "emoji_sad";
                client.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(client.getHabbo(), 59); // Efeito de chuva/nuvem
                break;
            case "festa":
                mensagem = "quer curtir! A festa começou! 🥳";
                icon = "emoji_party";
                client.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(client.getHabbo(), 163); // Efeito de confetes
                break;
            default:
                client.getHabbo().whisper("Humores disponíveis: feliz, bravo, triste, festa");
                return true;
        }

        // Faz o personagem "falar" a ação
        client.getHabbo().talk("* " + client.getHabbo().getHabboInfo().getUsername() + " " + mensagem + " *");

        // Envia um alerta visual para o próprio usuário confirmando o humor
        THashMap<String, String> keys = new THashMap<>();
        keys.put("title", "Seu Humor");
        keys.put("message", "Você definiu seu humor para: " + tipo.toUpperCase());
        client.getHabbo().getClient().sendResponse(new BubbleAlertComposer("hotel.event", keys));

        return true;
    }
}