package com.eu.habbo.habbohotel.wired;

public enum WiredTriggerType {
    SAY_SOMETHING(0),
    WALKS_ON_FURNI(1),
    WALKS_OFF_FURNI(2),
    AT_GIVEN_TIME(3),
    STATE_CHANGED(4),
    PERIODICALLY(6),
    ENTER_ROOM(7),
    SAIDOQUARTO(7),
    CLICAEMOUTROUSUARIO(7),
    USER_CLICKED(13),
    GAME_STARTS(8),
    GAME_ENDS(9),
    SCORE_ACHIEVED(10),
    COLLISION(11),
    PERIODICALLY_LONG(12),
    BOT_REACHED_STF(13),
    BOT_REACHED_AVTR(14),
    SAY_COMMAND(0),
    IDLES(11),
    UNIDLES(11),
    CUSTOM(13),
    AVISO(14),
    STARTS_DANCING(11),
    STOPS_DANCING(11),
    USER_CLICKS_USER(81),
    CLICKS_TILE( 85),
    USER_CLICKS_TILE(83),
    USER_PERFORMS_ACTION(91);


    public final int code;

    WiredTriggerType(int code) {
        this.code = code;
    }
}
