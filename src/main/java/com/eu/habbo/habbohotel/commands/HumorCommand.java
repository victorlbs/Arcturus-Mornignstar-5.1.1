package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.BubbleAlertComposer;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserEffectComposer; // Import necessário
import gnu.trove.map.hash.THashMap;

public class HumorCommand extends Command {
    public HumorCommand() {
        // Mudei de cmd_commands para cmd_humor para ficar correto na DB
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
        int effectId = 0;

        switch (tipo) {
            case "feliz":
                mensagem = "está se sentindo radiante hoje! ✨";
                effectId = 168; // Efeito de brilho
                break;
            case "bravo":
                mensagem = "está furioso! Saiam da frente! 💢";
                effectId = 25; // Efeito de fogo/raiva
                break;
            case "triste":
                mensagem = "está num clima melancólico... 🌧️";
                effectId = 59; // Efeito de chuva/nuvem
                break;
            case "festa":
                mensagem = "quer curtir! A festa começou! 🥳";
                effectId = 163; // Efeito de confetes
                break;
            default:
                client.getHabbo().whisper("Humores disponíveis: feliz, bravo, triste, festa");
                return true;
        }

        // Aplica o efeito usando os dois parâmetros exigidos pela sua RoomUnit
        if (effectId > 0) {
            client.getHabbo().getRoomUnit().setEffectId(effectId, -1);
            // Notifica o quarto sobre a mudança visual do usuário
            client.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserEffectComposer(client.getHabbo().getRoomUnit()).compose());
        }

        // Faz o personagem "falar" a ação
        client.getHabbo().talk("* " + client.getHabbo().getHabboInfo().getUsername() + " " + mensagem + " *");

        // Envia o balão de alerta visual
        THashMap<String, String> keys = new THashMap<>();
        keys.put("title", "Habbriol Humor");
        keys.put("message", "Seu humor agora é: " + tipo.toUpperCase());
        client.getHabbo().getClient().sendResponse(new BubbleAlertComposer("hotel.event", keys));

        return true;
    }
}