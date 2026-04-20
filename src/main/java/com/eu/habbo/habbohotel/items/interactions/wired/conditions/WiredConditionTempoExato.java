package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ClientMessage;
import com.eu.habbo.messages.ServerMessage;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredConditionTempoExato extends InteractionWiredCondition {
    private static final WiredConditionType type = WiredConditionType.TIME_MORE_THAN;

    private int cycles;

    public WiredConditionTempoExato(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredConditionTempoExato(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        return (Emulator.getIntUnixTimestamp() - room.getLastTimerReset()) / 0.5 > this.cycles;
    }

    @Override
    public String getWiredData() {
        // Calculando as horas, minutos e segundos a partir dos ciclos (segundos)
        int hours = this.cycles / 3600;
        int minutes = (this.cycles % 3600) / 60;
        int seconds = this.cycles % 60;

        // Retornando os dados como JSON com os três valores
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(hours, minutes, seconds));
    }

    static class JsonData {
        int hours;
        int minutes;
        int seconds;

        // Construtor que aceita horas, minutos e segundos
        public JsonData(int hours, int minutes, int seconds) {
            this.hours = hours;
            this.minutes = minutes;
            this.seconds = seconds;
        }
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");

        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                // Convertendo as horas, minutos e segundos para um total de segundos
                this.cycles = (data.hours * 3600) + (data.minutes * 60) + data.seconds;
            } else {
                if (!wiredData.equals(""))
                    this.cycles = Integer.parseInt(wiredData);
            }
        } catch (Exception e) {
        }
    }

    @Override
    public void onPickUp() {
        this.cycles = 0;
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        int hours = this.cycles / 3600;
        int minutes = (this.cycles % 3600) / 60;
        int seconds = this.cycles % 60;

        message.appendBoolean(false);
        message.appendInt(5);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");
        message.appendInt(1);
        message.appendInt(hours);    // Horas
        message.appendInt(minutes);  // Minutos
        message.appendInt(seconds);  // Segundos
        message.appendInt(0);
        message.appendInt(this.getType().code);
        message.appendInt(0);
        message.appendInt(0);
    }

    public boolean saveData(WiredSettings settings) {
        if (settings.getIntParams().length < 3) return false;

        // Captura as horas, minutos e segundos inseridos pelo usuário
        int hours = settings.getIntParams()[0];
        int minutes = settings.getIntParams()[1];
        int seconds = settings.getIntParams()[2];

        // Converte tudo para segundos
        this.cycles = (hours * 3600) + (minutes * 60) + seconds;

        return true;
    }
}
