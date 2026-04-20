package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import java.util.List;

public class CommandsCommand extends Command {

    public CommandsCommand() {
        super("cmd_commands", Emulator.getTexts().getValue("commands.keys.cmd_commands").split(";"));
    }

    @Override
    public boolean handle(GameClient gameClient, String[] params) throws Exception {
        List<Command> commands = Emulator.getGameEnvironment()
                .getCommandHandler()
                .getCommandsForRank(gameClient.getHabbo().getHabboInfo().getRank().getId());

        StringBuilder message = new StringBuilder();

        // Cabeçalho Centralizado com Emojis
        message.append("<center><b>✨ CENTRAL DE COMANDOS ✨</b></center>\r");
        message.append("<center>Você tem acesso a <b>").append(commands.size()).append("</b> funções!</center>\r");
        message.append("______________________________________________\r\n");

        // --- DICA: Criamos grupos visuais apenas com texto ---

        message.append("👤 <b>COMANDOS DE PERSONAGEM</b>\r");
        for (Command c : commands) {
            // Exemplo de filtro manual: se o comando for de animação ou estado do boneco
            if (isPersonagemCommand(c.keys[0])) {
                appendCmdLine(message, c);
            }
        }
        message.append("\r");

        message.append("🏠 <b>COMANDOS DE QUARTO</b>\r");
        for (Command c : commands) {
            if (isQuartoCommand(c.keys[0])) {
                appendCmdLine(message, c);
            }
        }
        message.append("\r");

        message.append("⚙️ <b>OUTROS COMANDOS</b>\r");
        for (Command c : commands) {
            if (!isPersonagemCommand(c.keys[0]) && !isQuartoCommand(c.keys[0])) {
                appendCmdLine(message, c);
            }
        }

        message.append("______________________________________________");

        gameClient.getHabbo().alert(message.toString());
        return true;
    }

    // Função auxiliar para formatar a linha de cada comando "bonitinho"
    private void appendCmdLine(StringBuilder sb, Command c) {
        String trigger = ":" + String.join(" / :", c.keys);
        String desc = Emulator.getTexts().getValue("commands.description." + c.permission, "Sem descrição.");

        sb.append("<font color='#0088cc'><b>▸ ").append(trigger).append("</b></font>\r");
        sb.append("      ").append(desc).append("\r");
    }

    // Lógica simples para separar o que é de Personagem
    private boolean isPersonagemCommand(String key) {
        String k = key.toLowerCase();
        return k.contains("sit") || k.contains("stand") || k.contains("dance") || k.contains("mimic") || k.contains("face") || k.contains("copy");
    }

    // Lógica simples para separar o que é de Quarto
    private boolean isQuartoCommand(String key) {
        String k = key.toLowerCase();
        return k.contains("floor") || k.contains("pick") || k.contains("bh") || k.contains("piso") || k.contains("room") || k.contains("furni");
    }
}