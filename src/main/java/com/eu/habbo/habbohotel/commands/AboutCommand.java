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
    public static String credits = "Arcturus Morningstar is an opensource project based on Arcturus By TheGeneral \n" +
            "The Following people have all contributed to this emulator:\n" +
            " TheGeneral\n Beny\n Alejandro\n Capheus\n Skeletor\n Harmonic\n Mike\n Remco\n zGrav \n Quadral \n Harmony\n Swirny\n ArpyAge\n Mikkel\n Rodolfo\n Rasmus\n Kitt Mustang\n Snaiker\n nttzx\n necmi\n Dome\n Jose Flores\n Cam\n Oliver\n Narzo\n Tenshie\n MartenM\n Ridge\n SenpaiDipper\n Snaiker\n Thijmen";
    @Override
    public boolean handle(GameClient gameClient, String[] params) {

        Emulator.getRuntime().gc();

        int seconds = Emulator.getIntUnixTimestamp() - Emulator.getTimeStarted();
        int day = (int) TimeUnit.SECONDS.toDays(seconds);
        long hours = TimeUnit.SECONDS.toHours(seconds) - (day * 24);
        long minute = TimeUnit.SECONDS.toMinutes(seconds) - (TimeUnit.SECONDS.toHours(seconds) * 60);
        long second = TimeUnit.SECONDS.toSeconds(seconds) - (TimeUnit.SECONDS.toMinutes(seconds) * 60);

        String message = "<b>" + Emulator.version + "</b>\r\n";

      //  AchievementManager.progressAchievement(this.starter, Emulator.getGameEnvironment().getAchievementManager().getAchievement("MusicPlayer"), (Emulator.getIntUnixTimestamp() - cycleStartedTimestamp) / 60);



        if (Emulator.getConfig().getBoolean("info.shown", true)) {
            message = "<font color=\"red\">E</font><font color=\"green\">g</font><font color=\"blue\">l</font><font color=\"purple\">a</font><font color=\"orange\">n</font><font color=\"cyan\">c</font><font color=\"magenta\">e</font> <font color=\"red\">S</font><font color=\"green\">e</font><font color=\"blue\">r</font><font color=\"purple\">v</font><font color=\"orange\">e</font><font color=\"cyan\">r</font> <font color=\"blue\">v.07042026</font> <font color=\"orange\">2026</font><br>" +
                    "<b>Estatisticas do Hotel</b>\r" +

            "- Usuarios online: " + Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "\r" +


                    "\n" +
                    "<b>Dados</b>\r" +
                    "- Tempo online: " + day + (day > 1 ? " dias, " : " dia, ") + hours + (hours > 1 ? " horas, " : " hora, ") + minute + (minute > 1 ? " minutos, " : " minuto, ") + second + (second > 1 ? " segundos!" : " segundo!") + "\r";
        }


        gameClient.getHabbo().alert(message);
        //gameClient.sendResponse(new MessagesForYouComposer(Collections.singletonList(credits)));
        return true;
    }
}
