package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.users.UserCreditsComposer;
import com.eu.habbo.messages.outgoing.users.UserPointsComposer;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectGiveCurrency extends InteractionWiredEffect {
    // Usamos o tipo genérico, mas abriremos a interface de texto (SHOW_MESSAGE)
    public static final WiredEffectType type = WiredEffectType.SHOW_MESSAGE;

    private int currencyType = 0; // 0 = Créditos, 5 = Diamantes, 101 = Pontos...
    private int amount = 0;

    public WiredEffectGiveCurrency(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectGiveCurrency(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Serializando Efeito: Dar Moedas");

        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());

        // Enviamos a configuração atual para a caixa de texto
        message.appendString(this.currencyType + ":" + this.amount);

        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(type.code); // Força a abrir a caixa de texto
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("============== DEBUG SAVE DAR MOEDAS ==============");
        String input = settings.getStringParam();

        if (input != null && input.contains(":")) {
            try {
                String[] parts = input.split(":");
                this.currencyType = Integer.parseInt(parts[0]);
                this.amount = Integer.parseInt(parts[1]);

                // Trava de segurança para não darem 1 milhão de moedas num clique
                if (this.amount > 50000) {
                    this.amount = 50000;
                }
                if (this.amount < 0) {
                    this.amount = 0;
                }

                System.out.println("[RESULTADO] Tipo de moeda: " + this.currencyType);
                System.out.println("[RESULTADO] Quantidade: " + this.amount);
            } catch (Exception e) {
                System.out.println("[!] Erro: Formato inválido. Use TIPO:QUANTIDADE");
            }
        } else {
            System.out.println("[!] Erro: Formato inválido. Digite por exemplo 5:10");
        }

        this.setDelay(settings.getDelay());
        System.out.println("===================================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE DAR MOEDAS ==============");
        if (this.amount <= 0) return false;

        Habbo habbo = room.getHabbo(roomUnit);

        if (habbo != null) {
            System.out.println("[INFO] Entregando " + this.amount + " (Moeda tipo " + this.currencyType + ") para " + habbo.getHabboInfo().getUsername());

            // 0 = Moedas / Créditos normais
            if (this.currencyType == 0) {
                habbo.getHabboInfo().addCredits(this.amount);
                habbo.getClient().sendResponse(new UserCreditsComposer(habbo));
            }
            // Qualquer outro valor (5 = Diamantes, 0 = Duckets, 101 = Pontos)
            else {
                habbo.getHabboInfo().addCurrencyAmount(this.currencyType, this.amount);
                habbo.getClient().sendResponse(new UserPointsComposer(habbo.getHabboInfo().getCurrencyAmount(this.currencyType), this.amount, this.currencyType));
            }

            System.out.println("[RESULTADO] Sucesso! Moedas entregues.");
            return true;
        }

        System.out.println("======================================================");
        return false;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.currencyType, this.amount, this.getDelay()));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.currencyType = data.currencyType;
                this.amount = data.amount;
                this.setDelay(data.delay);
            } else if (!wiredData.isEmpty()) {
                String[] data = wiredData.split(":");
                this.currencyType = Integer.parseInt(data[0]);
                this.amount = Integer.parseInt(data[1]);
            }
        } catch (Exception e) {
            System.out.println("[!] Erro ao carregar dados do Wired Dar Moedas.");
        }
    }

    @Override
    public void onPickUp() {
        this.amount = 0;
        this.currencyType = 0;
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    static class JsonData {
        int currencyType;
        int amount;
        int delay;

        public JsonData(int currencyType, int amount, int delay) {
            this.currencyType = currencyType;
            this.amount = amount;
            this.delay = delay;
        }
    }
}