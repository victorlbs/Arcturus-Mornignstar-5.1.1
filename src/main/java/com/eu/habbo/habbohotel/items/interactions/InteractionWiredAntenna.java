package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.habbohotel.wired.WiredTriggerType;

import java.sql.ResultSet;
import java.sql.SQLException;

public class InteractionWiredAntenna extends InteractionDefault {

    public InteractionWiredAntenna(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public InteractionWiredAntenna(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        // 1. Faz a animação normal do mobi (mudar extradata)
        super.onClick(client, room, objects);

        // 2. O PULO DO GATO: Dispara o sinal para os Wireds
        // Usamos o WiredHandler para procurar no quarto quem está ouvindo este mobi
        WiredHandler.handle(WiredTriggerType.STATE_CHANGED, client.getHabbo().getRoomUnit(), room, new Object[]{this});

        // Nota: O 'this' enviado no final é o que entra no 'stuff[0]' do seu Wired!
    }
}