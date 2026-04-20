package com.eu.habbo.habbohotel.items.interactions.wired.conditions;

import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredCondition;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.wired.WiredConditionType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.list.array.TIntArrayList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class WiredConditionHabboDirection extends InteractionWiredCondition {
    public static final WiredConditionType type = WiredConditionType.ACTOR_HAS_DIRECTION;

    private final TIntArrayList directions;

    public WiredConditionHabboDirection(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.directions = new TIntArrayList();
    }

    public WiredConditionHabboDirection(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.directions = new TIntArrayList();
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        message.appendBoolean(false);
        message.appendInt(0);
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");

        // Enviando a lista de inteiros (as direções selecionadas)
        message.appendInt(this.directions.size());
        for (int i = 0; i < this.directions.size(); i++) {
            message.appendInt(this.directions.get(i));
        }

        message.appendInt(0);

        // --- O SEGREDO ESTÁ NESTA LINHA ---
        // Forçamos a SWF a ler o código 7 (ACTOR_HAS_DIRECTION)
        message.appendInt(7);
        // ----------------------------------

        message.appendInt(0);
        message.appendInt(0);

        System.out.println("[DEBUG] Serializado com " + this.directions.size() + " direções selecionadas e Código 7.");
    }


    @Override
    public boolean saveData(WiredSettings settings) {
        System.out.println("============== DEBUG SAVE DIREÇÃO ==============");
        this.directions.clear();

        // No Wired de direção, as setas clicadas vêm como IntParams (0 a 7)
        int[] selected = settings.getIntParams();

        if (selected == null) {
            System.out.println("[!] Erro: Nenhum parâmetro recebido.");
            return false;
        }

        for (int dir : selected) {
            this.directions.add(dir);
            System.out.println("[INFO] Direção salva: " + dir);
        }

        System.out.println("[RESULTADO] Total de direções salvas: " + this.directions.size());
        System.out.println("================================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE DIREÇÃO ==============");
        if (roomUnit == null) return false;

        // Pega a rotação do corpo (Enum) e transforma em valor (0-7)
        int currentRotation = roomUnit.getBodyRotation().getValue();

        System.out.println("[Habbo] Está olhando para: " + currentRotation);
        System.out.println("[Wired] Direções permitidas: " + this.directions.toString());

        // Se o usuário não escolheu nenhuma direção, o wired deixa passar (padrão habbo)
        if (this.directions.isEmpty()) {
            System.out.println("[INFO] Nenhuma direção configurada. Passando...");
            return true;
        }

        boolean success = this.directions.contains(currentRotation);

        System.out.println("[RESULTADO] Condição aceita? " + (success ? "SIM" : "NÃO"));
        System.out.println("===================================================");

        return success;
    }

    @Override
    public String getWiredData() {
        // Converte o TIntArrayList para um array simples para o JSON
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.directions.toArray()));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.directions.clear();
        String wiredData = set.getString("wired_data");

        if (wiredData.startsWith("{")) {
            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
            if (data.directions != null) {
                this.directions.add(data.directions);
            }
        }
        System.out.println("[WIRED LOAD] Direções carregadas: " + this.directions.toString());
    }

    @Override
    public void onPickUp() {
        this.directions.clear();
    }

    @Override
    public WiredConditionType getType() {
        return type;
    }

    static class JsonData {
        int[] directions;
        public JsonData(int[] directions) {
            this.directions = directions;
        }
    }
}