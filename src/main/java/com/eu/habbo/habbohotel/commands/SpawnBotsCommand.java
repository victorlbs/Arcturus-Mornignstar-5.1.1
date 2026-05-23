package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import gnu.trove.map.hash.THashMap;

public class SpawnBotsCommand extends Command {

    public SpawnBotsCommand() {
        super("cmd_alert", new String[]{"spawnbot", "sbot"}); }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        if (params.length < 2) {
            gameClient.getHabbo().whisper("Uso correto: :spawnbots <quantidade>");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(params[1]);
        } catch (NumberFormatException e) {
            gameClient.getHabbo().whisper("Por favor, digite um número válido.");
            return true;
        }

        // Limite de segurança para não travar o emulador
        if (amount <= 0 || amount > 50) {
            gameClient.getHabbo().whisper("Para evitar lag, a quantidade deve ser entre 1 e 50.");
            return true;
        }

        Room room = gameClient.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null) return false;

        // Verifica se é o dono do quarto ou Staff
        if (room.getOwnerId() != gameClient.getHabbo().getHabboInfo().getId() && !gameClient.getHabbo().hasPermission("acc_anyroomowner")) {
            gameClient.getHabbo().whisper("Você só pode spawnar bots nos seus próprios quartos.");
            return true;
        }

        String username = gameClient.getHabbo().getHabboInfo().getUsername() + " Clone";
        String figure = gameClient.getHabbo().getHabboInfo().getLook();
        String gender = gameClient.getHabbo().getHabboInfo().getGender().name();

        int botsCreated = 0;
        int currentX = gameClient.getHabbo().getRoomUnit().getX();
        int currentY = gameClient.getHabbo().getRoomUnit().getY();

        for (int i = 0; i < amount; i++) {
            RoomTile spawnTile = null;

            // Sistema de "Radar": Procura num raio de até 15 blocos de distância um piso vazio para colocar o bot
            buscar_piso_livre:
            for (int raio = 1; raio <= 15; raio++) {
                for (int xOffset = -raio; xOffset <= raio; xOffset++) {
                    for (int yOffset = -raio; yOffset <= raio; yOffset++) {
                        RoomTile t = room.getLayout().getTile((short)(currentX + xOffset), (short)(currentY + yOffset));

                        // Se o piso existe, dá pra andar, NÃO tem habbo e NÃO tem bot em cima, achamos o lugar perfeito!
                        if (t != null && t.isWalkable() && !room.hasHabbosAt(t.x, t.y) && !room.hasBotsAt(t.x, t.y)) {
                            spawnTile = t;
                            break buscar_piso_livre;
                        }
                    }
                }
            }

            // Se não encontrou nenhum piso livre (quarto muito pequeno ou lotado), cancela o resto.
            if (spawnTile == null) {
                gameClient.getHabbo().whisper("Não há mais pisos livres perto de você para spawnar bots!");
                break;
            }

            // Cria as informações do bot
            THashMap<String, String> botData = new THashMap<>();
            botData.put("name", username);
            botData.put("motto", "Clone Automático");
            botData.put("figure", figure);
            botData.put("gender", gender);

            Bot bot = Emulator.getGameEnvironment().getBotManager().createBot(botData, "generic");

            if (bot != null) {
                bot.setOwnerId(gameClient.getHabbo().getHabboInfo().getId());
                bot.setOwnerName(gameClient.getHabbo().getHabboInfo().getUsername());
                bot.needsUpdate(true);

                // Coloca o bot exatamente no piso livre que o radar encontrou
                Emulator.getGameEnvironment().getBotManager().placeBot(bot, gameClient.getHabbo(), room, spawnTile);
                botsCreated++;
            }
        }

        if (botsCreated > 0) {
            gameClient.getHabbo().whisper("🤖 Você spawnou " + botsCreated + " bots no quarto com sucesso!");
        }

        return true;
    }
}