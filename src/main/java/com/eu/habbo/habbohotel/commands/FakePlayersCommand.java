package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import gnu.trove.map.hash.THashMap;

import java.util.Random;

public class FakePlayersCommand extends Command {

    // Listas de nomes e roupas para gerar aleatoriedade
    private final String[] nomesBase = {"Matheus", "Sofia", "Lucas", "Isabela", "Gabriel", "Camila", "Rafael", "Amanda", "Bruno", "Leticia", "Felipe", "Beatriz", "Thiago", "Julia", "Pedro", "Mariana", "Joao", "Larissa", "Guilherme", "Aline"};

    // Roupas realistas misturadas (M e F)
    private final String[] looksMasculinos = {
            "hr-115-42.hd-190-1.ch-210-66.lg-270-82.sh-290-91",
            "hr-893-45.hd-180-1.ch-210-1408.lg-275-64.sh-290-62",
            "hr-3163-45.hd-180-2.ch-220-1408.lg-280-110.sh-300-64",
            "hr-100-34.hd-209-8.ch-255-92.lg-285-89.sh-295-62"
    };

    private final String[] looksFemininos = {
            "hr-515-45.hd-600-1.ch-630-82.lg-716-66.sh-730-62",
            "hr-540-34.hd-600-1.ch-665-92.lg-720-82.sh-735-68",
            "hr-3142-42.hd-600-2.ch-3030-1408.lg-3216-110.sh-3115-92",
            "hr-828-45.hd-600-1.ch-630-79.lg-700-82.sh-725-62"
    };

    public FakePlayersCommand() {
        super("cmd_alert", new String[]{"fakeplay", "fplay"});
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length < 2) {
            gameClient.getHabbo().whisper("Uso correto: :fakeplayers <quantidade>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(params[1]);
        } catch (NumberFormatException e) {
            gameClient.getHabbo().whisper("Por favor, digite um número válido.");
            return true;
        }

        if (amount <= 0 || amount > 50) {
            gameClient.getHabbo().whisper("Para evitar lag, a quantidade deve ser entre 1 e 50.");
            return true;
        }

        Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) return false;

        if (room.getOwnerId() != gameClient.getHabbo().getHabboInfo().getId() && !gameClient.getHabbo().hasPermission("acc_anyroomowner")) {
            gameClient.getHabbo().whisper("Você só pode usar isso nos seus próprios quartos.");
            return true;
        }

        int botsCreated = 0;
        int currentX = gameClient.getHabbo().getRoomUnit().getX();
        int currentY = gameClient.getHabbo().getRoomUnit().getY();
        Random random = new Random();

        for (int i = 0; i < amount; i++) {
            RoomTile spawnTile = null;

            // Radar de piso livre
            buscar_piso_livre:
            for (int raio = 1; raio <= 15; raio++) {
                for (int xOffset = -raio; xOffset <= raio; xOffset++) {
                    for (int yOffset = -raio; yOffset <= raio; yOffset++) {
                        RoomTile t = room.getLayout().getTile((short)(currentX + xOffset), (short)(currentY + yOffset));
                        if (t != null && t.isWalkable() && !room.hasHabbosAt(t.x, t.y) && !room.hasBotsAt(t.x, t.y)) {
                            spawnTile = t;
                            break buscar_piso_livre;
                        }
                    }
                }
            }

            if (spawnTile == null) {
                gameClient.getHabbo().whisper("Não há mais pisos livres para adicionar fakes!");
                break;
            }

            // Sorteia se vai ser homem (0) ou mulher (1)
            boolean isFemale = random.nextBoolean();

            // Cria um nome realista misturando um nome base com 2 números no final
            String randomName = nomesBase[random.nextInt(nomesBase.length)] + random.nextInt(99);
            String look = isFemale ? looksFemininos[random.nextInt(looksFemininos.length)] : looksMasculinos[random.nextInt(looksMasculinos.length)];
            String gender = isFemale ? "F" : "M";

            THashMap<String, String> botData = new THashMap<>();
            botData.put("name", randomName);
            botData.put("motto", "Offline"); // Missão neutra para parecer player AFK
            botData.put("figure", look);
            botData.put("gender", gender);

            Bot bot = Emulator.getGameEnvironment().getBotManager().createBot(botData, "generic");

            if (bot != null) {
                bot.setOwnerId(gameClient.getHabbo().getHabboInfo().getId());
                bot.setOwnerName(gameClient.getHabbo().getHabboInfo().getUsername());
                bot.needsUpdate(true);

                Emulator.getGameEnvironment().getBotManager().placeBot(bot, gameClient.getHabbo(), room, spawnTile);
                botsCreated++;
            }
        }

        if (botsCreated > 0) {
            gameClient.getHabbo().whisper("👥 Você adicionou " + botsCreated + " jogadores fakes no quarto!");
        }

        return true;
    }
}