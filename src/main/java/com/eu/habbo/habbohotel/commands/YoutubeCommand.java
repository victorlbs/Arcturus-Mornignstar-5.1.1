package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;

public class YoutubeCommand extends Command {

    public YoutubeCommand() {
        super("cmd_youtube", Emulator.getTexts().getValue("commands.keys.cmd_youtube").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) {
        if (params.length < 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_youtube.forgot_message"), RoomChatMessageBubbles.ALERT);
            return true;
        }

        StringBuilder message = new StringBuilder();

        for (int i = 1; i < params.length; i++) {
            message.append(params[i]).append(" ");
        }

        // Adicione o iframe do YouTube à mensagem
        String videoId = extractYouTubeVideoId(message.toString());
        if (videoId != null) {
            message.append("\r\n<iframe width=\"560\" height=\"315\" src=\"https://www.youtube.com/embed/" + videoId + "\" frameborder=\"0\" allowfullscreen></iframe>");
        } else {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_youtube.invalid_link"), RoomChatMessageBubbles.ALERT);
            return true;
        }

        // Fazer algo com a mensagem completa (message)
        String finalCmd = "YOUTUBE_VIDEO:" + videoId;
       // gameClient.getHabbo().chat(finalCmd, RoomChatMessageBubbles.BOT, 0);
        return true;
    }

    // Método para extrair o ID do vídeo do YouTube
    private String extractYouTubeVideoId(String message) {
        String[] parts = message.split("=");
        if (parts.length > 1) {
            return parts[1].trim();
        }
        return null;
    }
}
