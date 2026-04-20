package com.eu.habbo.habbohotel.items.interactions.wired.effects; // <-- ESTA É A LINHA QUE FALTA!

import com.eu.habbo.Emulator;

import com.eu.habbo.habbohotel.gameclients.GameClient;

import com.eu.habbo.habbohotel.items.Item;

import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;

import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;

import com.eu.habbo.habbohotel.rooms.Room;

import com.eu.habbo.habbohotel.rooms.RoomTile;

import com.eu.habbo.habbohotel.rooms.RoomUnit;

import com.eu.habbo.habbohotel.users.HabboItem;

import com.eu.habbo.habbohotel.wired.WiredEffectType;

import com.eu.habbo.habbohotel.wired.WiredHandler;

import com.eu.habbo.messages.ServerMessage;

import com.eu.habbo.messages.incoming.wired.WiredSaveException;

import gnu.trove.set.hash.THashSet;



import java.sql.ResultSet;

import java.sql.SQLException;

import java.util.ArrayList;

import java.util.List;

import java.util.stream.Collectors;



public class WiredEffectMoveMobiToMobi extends InteractionWiredEffect {

// Usamos um tipo padrão que permite selecionar mobis na interface do jogo

    public static final WiredEffectType type = WiredEffectType.TOGGLE_STATE;

    private final List<HabboItem> items = new ArrayList<>();



    public WiredEffectMoveMobiToMobi(ResultSet set, Item baseItem) throws SQLException {

        super(set, baseItem);

    }



    public WiredEffectMoveMobiToMobi(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {

        super(id, userId, item, extradata, limitedStack, limitedSells);

    }



    @Override

    public WiredEffectType getType() {

        return type;

    }



    @Override

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {

// Limpa mobis que já não estão no quarto

        this.items.removeIf(item -> room.getHabboItem(item.getId()) == null);



// A mágica: Se exatamente 2 mobis foram selecionados

        if (this.items.size() == 2) {

            HabboItem mobiQueVaiMover = this.items.get(0);

            HabboItem mobiDestino = this.items.get(1);



            if (mobiQueVaiMover != null && mobiDestino != null) {

// No Morningstar, precisamos "pegar" o quadrado do chão (RoomTile) onde o destino está

                RoomTile tileDestino = room.getLayout().getTile(mobiDestino.getX(), mobiDestino.getY());



                if (tileDestino != null) {

// Usamos a função nativa do teu emulador para mover de forma limpa e sem bugar a altura!

// Os parâmetros são: o item que move, o piso de destino, a rotação (mantemos a mesma), e quem moveu (null = o sistema/wired)

                    room.moveFurniTo(mobiQueVaiMover, tileDestino, mobiQueVaiMover.getRotation(), null);

                    return true;

                }

            }

        }

        return false;

    }



    @Override

    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {

        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());

        if (room == null) return false;



        this.items.clear();



// Bloqueia se tentar salvar mais de 2 mobis (Apenas origem e destino)

        if (settings.getFurniIds().length > 2) {

            throw new WiredSaveException("Apenas 2 mobis podem ser selecionados.");

        }



        for (int i = 0; i < settings.getFurniIds().length; i++) {

            HabboItem item = room.getHabboItem(settings.getFurniIds()[i]);

            if (item != null) {

                this.items.add(item);

            }

        }



        this.setDelay(settings.getDelay());

        return true;

    }



    @Override

    public String getWiredData() {

        this.items.removeIf(item -> item.getRoomId() != this.getRoomId() || Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(item.getId()) == null);



// Salva os dados em formato JSON

        return WiredHandler.getGsonBuilder().create().toJson(new JsonData(

                this.getDelay(),

                this.items.stream().map(HabboItem::getId).collect(Collectors.toList())

        ));

    }



    @Override

    public void serializeWiredData(ServerMessage message, Room room) {

        this.items.removeIf(item -> item.getRoomId() != this.getRoomId() || room.getHabboItem(item.getId()) == null);



        message.appendBoolean(false);

        message.appendInt(2); // Limite máximo de seleção: 2 mobis

        message.appendInt(this.items.size());



        for (HabboItem item : this.items) {

            message.appendInt(item.getId());

        }



        message.appendInt(this.getBaseItem().getSpriteId());

        message.appendInt(this.getId());

        message.appendString("");

        message.appendInt(0);

        message.appendInt(0);

        message.appendInt(this.getType().code);

        message.appendInt(this.getDelay());

        message.appendInt(0);

    }



    @Override

    public void loadWiredData(ResultSet set, Room room) throws SQLException {

        this.items.clear();

        String wiredData = set.getString("wired_data");



        if (wiredData != null && wiredData.startsWith("{")) {

            JsonData data = WiredHandler.getGsonBuilder().create().fromJson(wiredData, JsonData.class);

            this.setDelay(data.delay);



            for (Integer id: data.itemIds) {

                HabboItem item = room.getHabboItem(id);

                if (item != null) {

                    this.items.add(item);

                }

            }

        }

    }



    @Override

    public void onPickUp() {

        this.setDelay(0);

        this.items.clear();

    }



    @Override

    protected long requiredCooldown() {

        return 495;

    }



// Estrutura para salvar no banco de dados

    static class JsonData {

        int delay;

        List<Integer> itemIds;



        public JsonData(int delay, List<Integer> itemIds) {

            this.delay = delay;

            this.itemIds = itemIds;

        }

    }

}