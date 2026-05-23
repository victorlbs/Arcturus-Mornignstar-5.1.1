package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.users.HabboManager;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class AboutCommand extends Command {

    public AboutCommand() {
        super(null, new String[]{"about", "info", "online", "server"});
    }

    public static String credits = "Rio Emulador é um projeto baseado no Arcturus Morningstar / Arcturus by TheGeneral.\n" +
            "As seguintes pessoas contribuíram para a base deste emulador:\n" +
            "TheGeneral\n Beny\n Alejandro\n Capheus\n Skeletor\n Harmonic\n Mike\n Remco\n zGrav \n Quadral \n Harmony\n Swirny\n ArpyAge\n Mikkel\n Rodolfo\n Rasmus\n Kitt Mustang\n Snaiker\n nttzx\n necmi\n Dome\n Jose Flores\n Cam\n Oliver\n Narzo\n Tenshie\n MartenM\n Ridge\n SenpaiDipper\n Snaiker\n Thijmen";

    @Override
    public boolean handle(GameClient gameClient, String[] params) {

        // NÃO RECOMENDADO: Forçar o Garbage Collector do Java toda vez que o comando é rodado causa pequenos travamentos (lag spikes) no servidor.
        // O Java já faz o gerenciamento de memória automaticamente.
        // Emulator.getRuntime().gc();

        int seconds = Emulator.getIntUnixTimestamp() - Emulator.getTimeStarted();
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        String message = "<b>Rio Emulador (" + Emulator.version + ")</b>\r\n";

        if (Emulator.getConfig().getBoolean("info.shown", true)) {
            message = "<b><font color=\"#00BFFF\">Rio</font> <font color=\"#32CD32\">Emulador</font></b> <font color=\"#808080\">v.12052026</font> <font color=\"#FF8C00\">2026</font><br><br>" +
                    "<b>Estatísticas do Hotel</b>\r" +
                    "- Usuários online: " + Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "\r" +
                    "\n" +
                    "<b>Dados do Servidor</b>\r" +
                    "- Tempo online: " +
                    day + (day == 1 ? " dia, " : " dias, ") +
                    hours + (hours == 1 ? " hora, " : " horas, ") +
                    minute + (minute == 1 ? " minuto, " : " minutos, ") +
                    second + (second == 1 ? " segundo!" : " segundos!") + "\r";
        }

        // Envia o alerta principal
        gameClient.getHabbo().alert(message);

        // Se quiser enviar também os créditos numa caixa de mensagens, basta descomentar a linha abaixo:
        // gameClient.sendResponse(new MessagesForYouComposer(Collections.singletonList(credits)));

        return true;
    }
}