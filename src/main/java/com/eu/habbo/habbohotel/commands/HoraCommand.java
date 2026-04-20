package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import java.text.SimpleDateFormat;
import java.util.Date;

public class HoraCommand extends Command {
    public HoraCommand() {
        super(null, new String[]{"hora", "time"});

        // Adiciona textos à tabela emulator_texts, caso não existam
        verificarEAdicionarTexto("commands.keys.cmd_hora", "hora");
        verificarEAdicionarTexto("commands.description.cmd_hora", "Mostra a hora atual.");
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String horaAtual = sdf.format(new Date());

        // Envia a hora atual para o jogador
        gameClient.getHabbo().talk("A hora atual é: " + horaAtual, RoomChatMessageBubbles.YELLOW);

        return true;
    }

    /**
     * Verifica se uma chave existe na tabela emulator_texts e adiciona caso não exista.
     * @param key A chave a ser verificada.
     * @param defaultValue O valor padrão caso a chave não exista.
     */
    private void verificarEAdicionarTexto(String key, String defaultValue) {
        // Verifica se a chave já existe
        if (Emulator.getTexts().getValue(key, null) == null) {
            // Registra a nova chave no banco de dados
            Emulator.getTexts().register(key, defaultValue);
            Emulator.getLogging().logStart("Adicionada chave na tabela emulator_texts: " + key + " -> " + defaultValue);
        }
    }
}
