package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;

import java.sql.ResultSet;
import java.sql.SQLException;

public class WiredEffectMoveRotateUser extends InteractionWiredEffect {
    // Usamos o tipo MOVE_ROTATE para a SWF abrir a janela com as setinhas
    public static final WiredEffectType type = WiredEffectType.MOVE_ROTATE;

    private int direction = 0;
    private int rotation = 0;

    public WiredEffectMoveRotateUser(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
    }

    public WiredEffectMoveRotateUser(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
    }

    @Override
    public void serializeWiredData(ServerMessage message, Room room) {
        System.out.println("[WIRED DEBUG] Serializando Move/Gira Usuário");

        message.appendBoolean(false);
        message.appendInt(0); // 0 = NÃO seleciona mobis (É focado no usuário)
        message.appendInt(0);
        message.appendInt(this.getBaseItem().getSpriteId());
        message.appendInt(this.getId());
        message.appendString("");

        message.appendInt(2); // 2 Parâmetros da interface (Movimento e Rotação)
        message.appendInt(this.direction);
        message.appendInt(this.rotation);

        message.appendInt(0);
        message.appendInt(type.code); // Força abrir a interface de Move/Rotate
        message.appendInt(this.getDelay());
        message.appendInt(0);
    }

    @Override
    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        System.out.println("============== DEBUG SAVE MOVE USUÁRIO ==============");
        if(settings.getIntParams().length >= 2) {
            this.direction = settings.getIntParams()[0];
            this.rotation = settings.getIntParams()[1];

            System.out.println("[RESULTADO] Direção Salva: " + this.direction);
            System.out.println("[RESULTADO] Rotação Salva: " + this.rotation);
        }

        this.setDelay(settings.getDelay());
        System.out.println("=====================================================");
        return true;
    }

    @Override
    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        System.out.println("============== DEBUG EXECUTE MOVE USUÁRIO ==============");
        if (roomUnit == null || room == null) {
            System.out.println("[!] Falhou: Quarto ou Usuário nulo.");
            return false;
        }

        boolean acted = false;

        // 1. LÓGICA DE ROTAÇÃO (GIRAR)
        if (this.rotation > 0) {
            int currentRot = roomUnit.getBodyRotation().getValue();
            int newRot = currentRot;

            // No Habbo, a rotação vai de 0 a 7. Cada giro de 90 graus = +2 ou -2
            if (this.rotation == 1) { // Sentido Horário
                newRot = (currentRot + 2) % 8;
            } else if (this.rotation == 2) { // Sentido Anti-Horário
                newRot = (currentRot + 6) % 8; // +6 é o mesmo que -2 matematicamente
            } else if (this.rotation == 3) { // Aleatório
                newRot = Emulator.getRandom().nextInt(8);
            }

            roomUnit.setRotation(RoomUserRotation.values()[newRot]);
            roomUnit.statusUpdate(true);
            System.out.println("[INFO] Usuário girou para a posição: " + newRot);
            acted = true;
        }

        // 2. LÓGICA DE MOVIMENTO (ANDAR)
        if (this.direction > 0) {
            int moveDir = this.direction;

            // Resolve as direções aleatórias (5, 6 e 7)
            if (moveDir == 5) moveDir = 1 + Emulator.getRandom().nextInt(4); // 1, 2, 3 ou 4
            else if (moveDir == 6) moveDir = Emulator.getRandom().nextBoolean() ? 1 : 3; // Cima/Baixo
            else if (moveDir == 7) moveDir = Emulator.getRandom().nextBoolean() ? 2 : 4; // Esquerda/Direita

            short newX = roomUnit.getX();
            short newY = roomUnit.getY();

            if (moveDir == 1) newY--;      // Cima (Norte)
            else if (moveDir == 2) newX++; // Direita (Leste)
            else if (moveDir == 3) newY++; // Baixo (Sul)
            else if (moveDir == 4) newX--; // Esquerda (Oeste)

            // Verifica se o quadrado para onde ele vai está livre para andar
            if (room.tileWalkable(newX, newY)) {
                System.out.println("[INFO] Teleportando usuário para: X=" + newX + ", Y=" + newY);
                // O Room.java já tem um método seguro para teleportar o usuário
                room.teleportRoomUnitToLocation(roomUnit, newX, newY);
                acted = true;
            } else {
                System.out.println("[INFO] Movimento bloqueado. O quadrado destino não está livre.");
            }
        }

        System.out.println("========================================================");
        return acted;
    }

    @Override
    public String getWiredData() {
        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(this.direction, this.rotation, this.getDelay()));
    }

    @Override
    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        String wiredData = set.getString("wired_data");
        try {
            if (wiredData.startsWith("{")) {
                JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);
                this.direction = data.direction;
                this.rotation = data.rotation;
                this.setDelay(data.delay);
            } else if (!wiredData.isEmpty()) {
                String[] data = wiredData.split("\t");
                this.direction = Integer.parseInt(data[0]);
                this.rotation = Integer.parseInt(data[1]);
                if (data.length > 2) this.setDelay(Integer.parseInt(data[2]));
            }
        } catch (Exception e) {
            System.out.println("[!] Erro ao carregar os dados do Move/Gira Usuário.");
        }
    }

    @Override
    public void onPickUp() {
        this.direction = 0;
        this.rotation = 0;
        this.setDelay(0);
    }

    @Override
    public WiredEffectType getType() {
        return type;
    }

    static class JsonData {
        int direction;
        int rotation;
        int delay;

        public JsonData(int direction, int rotation, int delay) {
            this.direction = direction;
            this.rotation = rotation;
            this.delay = delay;
        }
    }
}