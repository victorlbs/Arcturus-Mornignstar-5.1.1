package com.eu.habbo.habbohotel.items.interactions.wired.effects;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.ICycleable;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.items.interactions.InteractionWiredEffect;
import com.eu.habbo.habbohotel.items.interactions.wired.WiredSettings;
import com.eu.habbo.habbohotel.rooms.FurnitureMovementError;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomLayout;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomTileState;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.habbohotel.wired.WiredEffectType;
import com.eu.habbo.habbohotel.wired.WiredHandler;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.wired.WiredSaveException;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;
import com.eu.habbo.threading.runnables.WiredCollissionRunnable;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WiredEffectMoveRotateCollideFurni extends InteractionWiredEffect implements ICycleable {
    private static final Logger LOGGER = LoggerFactory.getLogger(WiredEffectMoveRotateCollideFurni.class);
    public static final WiredEffectType type;
    private final THashSet<HabboItem> items;
    private int direction;
    private int rotation;
    private THashSet<HabboItem> itemCooldowns;

    public WiredEffectMoveRotateCollideFurni(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.items = new THashSet(WiredHandler.MAXIMUM_FURNI_SELECTION / 2);
        this.itemCooldowns = new THashSet();
    }

    public WiredEffectMoveRotateCollideFurni(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.items = new THashSet(WiredHandler.MAXIMUM_FURNI_SELECTION / 2);
        this.itemCooldowns = new THashSet();
    }

    public boolean execute(RoomUnit roomUnit, Room room, Object[] stuff) {
        this.items.removeIf((itemx) -> {
            return Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(itemx.getId()) == null;
        });
        TObjectHashIterator var4 = this.items.iterator();

        label171:
        while(true) {
            HabboItem item;
            do {
                if (!var4.hasNext()) {
                    return true;
                }

                item = (HabboItem)var4.next();
            } while(this.itemCooldowns.contains(item));

            int newRotation = this.rotation > 0 ? this.getNewRotation(item) : item.getRotation();
            RoomTile newLocation = room.getLayout().getTile(item.getX(), item.getY());
            RoomTile oldLocation = room.getLayout().getTile(item.getX(), item.getY());
            double oldZ = item.getZ();
            if (this.direction > 0) {
                RoomUserRotation moveDirection = this.getMovementDirection();
                newLocation = room.getLayout().getTile((short)(item.getX() + (moveDirection != RoomUserRotation.WEST && moveDirection != RoomUserRotation.NORTH_WEST && moveDirection != RoomUserRotation.SOUTH_WEST ? (moveDirection != RoomUserRotation.EAST && moveDirection != RoomUserRotation.SOUTH_EAST && moveDirection != RoomUserRotation.NORTH_EAST ? 0 : 1) : -1)), (short)(item.getY() + (moveDirection != RoomUserRotation.NORTH && moveDirection != RoomUserRotation.NORTH_EAST && moveDirection != RoomUserRotation.NORTH_WEST ? (moveDirection != RoomUserRotation.SOUTH && moveDirection != RoomUserRotation.SOUTH_EAST && moveDirection != RoomUserRotation.SOUTH_WEST ? 0 : -1) : 1)));
            }

            boolean slideAnimation = item.getRotation() == newRotation;
            FurnitureMovementError furniMoveTest = room.furnitureFitsAt(newLocation, item, newRotation, true);
            if (newLocation != null && newLocation.state != RoomTileState.INVALID && (newLocation != oldLocation || newRotation != item.getRotation()) && (furniMoveTest == FurnitureMovementError.NONE || (furniMoveTest == FurnitureMovementError.TILE_HAS_BOTS || furniMoveTest == FurnitureMovementError.TILE_HAS_HABBOS || furniMoveTest == FurnitureMovementError.TILE_HAS_PETS) && newLocation == oldLocation) && room.furnitureFitsAt(newLocation, item, newRotation, false) == FurnitureMovementError.NONE && room.moveFurniTo(item, newLocation, newRotation, (Habbo)null, !slideAnimation) == FurnitureMovementError.NONE) {
                this.itemCooldowns.add(item);
                if (slideAnimation) {
                    room.sendComposer((new FloorItemOnRollerComposer(item, (HabboItem)null, oldLocation, oldZ, newLocation, item.getZ(), 0.0D, room)).compose());
                }
            }

            RoomLayout layout = room.getLayout();
            RoomTile startTile = layout.getTile(item.getX(), item.getY());
            THashSet<RoomTile> tiles = layout.getTilesAt(startTile, item.getBaseItem().getWidth(), item.getBaseItem().getLength(), item.getRotation());
            THashSet<RoomTile> allTiles = new THashSet();
            TObjectHashIterator var17 = tiles.iterator();

            while(var17.hasNext()) {
                RoomTile tile = (RoomTile)var17.next();
                if (!allTiles.contains(tile)) {
                    allTiles.add(tile);
                }

                Iterator var19 = layout.getTilesAround(tile).iterator();

                while(var19.hasNext()) {
                    RoomTile newTile = (RoomTile)var19.next();
                    if (!allTiles.contains(newTile)) {
                        allTiles.add(newTile);
                    }
                }
            }

            List<Habbo> habbos = new ArrayList();
            TObjectHashIterator var24 = allTiles.iterator();

            while(true) {
                RoomTile tile;
                do {
                    do {
                        if (!var24.hasNext()) {
                            Iterator var25 = habbos.iterator();

                            while(var25.hasNext()) {
                                Habbo habbo = (Habbo)var25.next();
                                Emulator.getThreading().run(new WiredCollissionRunnable(habbo.getRoomUnit(), room, new Object[]{item}));
                            }
                            continue label171;
                        }

                        tile = (RoomTile)var24.next();
                    } while(tile == null);
                } while(!layout.tileExists(tile.x, tile.y));

                TObjectHashIterator var28 = room.getHabbosAt(tile).iterator();

                while(var28.hasNext()) {
                    Habbo habbo = (Habbo)var28.next();
                    if (!habbos.contains(habbo)) {
                        habbos.add(habbo);
                    }
                }
            }
        }
    }

    public String getWiredData() {
        THashSet<HabboItem> itemsToRemove = new THashSet(this.items.size() / 2);
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
        TObjectHashIterator var3 = this.items.iterator();

        while(true) {
            HabboItem item;
            do {
                if (!var3.hasNext()) {
                    var3 = itemsToRemove.iterator();

                    while(var3.hasNext()) {
                        item = (HabboItem)var3.next();
                        this.items.remove(item);
                    }

                    return WiredHandler.getGsonBuilder().create().toJson(new WiredEffectMoveRotateCollideFurni.JsonData(this.direction, this.rotation, this.getDelay(), (List)this.items.stream().map(HabboItem::getId).collect(Collectors.toList())));
                }

                item = (HabboItem)var3.next();
            } while(item.getRoomId() == this.getRoomId() && (room == null || room.getHabboItem(item.getId()) != null));

            itemsToRemove.add(item);
        }
    }

    public void loadWiredData(ResultSet set, Room room) throws SQLException {
        this.items.clear();
        String wiredData = set.getString("wired_data");
        if (wiredData.startsWith("{")) {
            WiredEffectMoveRotateCollideFurni.JsonData data = (WiredEffectMoveRotateCollideFurni.JsonData)WiredHandler.getGsonBuilder().create().fromJson(wiredData, WiredEffectMoveRotateCollideFurni.JsonData.class);
            this.setDelay(data.delay);
            this.direction = data.direction;
            this.rotation = data.rotation;
            Iterator var5 = data.itemIds.iterator();

            while(var5.hasNext()) {
                Integer id = (Integer)var5.next();
                HabboItem item = room.getHabboItem(id);
                if (item != null) {
                    this.items.add(item);
                }
            }
        } else {
            String[] data = wiredData.split("\t");
            if (data.length == 4) {
                try {
                    this.direction = Integer.parseInt(data[0]);
                    this.rotation = Integer.parseInt(data[1]);
                    this.setDelay(Integer.parseInt(data[2]));
                } catch (Exception var10) {
                    System.out.println(var10);
                }

                String[] var12 = data[3].split("\r");
                int var13 = var12.length;

                for(int var14 = 0; var14 < var13; ++var14) {
                    String s = var12[var14];
                    HabboItem item = room.getHabboItem(Integer.parseInt(s));
                    if (item != null) {
                        this.items.add(item);
                    }
                }
            }
        }

    }

    public void onPickUp() {
        this.direction = 0;
        this.rotation = 0;
        this.items.clear();
        this.setDelay(0);
    }

    public WiredEffectType getType() {
        return type;
    }

    public void serializeWiredData(ServerMessage message, Room room) {
        THashSet<HabboItem> items = new THashSet();
        TObjectHashIterator var4 = this.items.iterator();

        while(true) {
            HabboItem item;
            do {
                if (!var4.hasNext()) {
                    var4 = items.iterator();

                    while(var4.hasNext()) {
                        item = (HabboItem)var4.next();
                        this.items.remove(item);
                    }

                    message.appendBoolean(false);
                    message.appendInt(WiredHandler.MAXIMUM_FURNI_SELECTION);
                    message.appendInt(this.items.size());
                    var4 = this.items.iterator();

                    while(var4.hasNext()) {
                        item = (HabboItem)var4.next();
                        message.appendInt(item.getId());
                    }

                    message.appendInt(this.getBaseItem().getSpriteId());
                    message.appendInt(this.getId());
                    message.appendString("");
                    message.appendInt(2);
                    message.appendInt(this.direction);
                    message.appendInt(this.rotation);
                    message.appendInt(0);
                    message.appendInt(this.getType().code);
                    message.appendInt(this.getDelay());
                    message.appendInt(0);
                    return;
                }

                item = (HabboItem)var4.next();
            } while(item.getRoomId() == this.getRoomId() && Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId()).getHabboItem(item.getId()) != null);

            items.add(item);
        }
    }

    public boolean saveData(WiredSettings settings, GameClient gameClient) throws WiredSaveException {
        Room room = Emulator.getGameEnvironment().getRoomManager().getRoom(this.getRoomId());
        if (room == null) {
            return false;
        } else if (settings.getIntParams().length < 2) {
            throw new WiredSaveException("invalid data");
        } else {
            this.direction = settings.getIntParams()[0];
            this.rotation = settings.getIntParams()[1];
            int count = settings.getFurniIds().length;
            if (count > Emulator.getConfig().getInt("hotel.wired.furni.selection.count", 5)) {
                return false;
            } else {
                this.items.clear();

                for(int i = 0; i < count; ++i) {
                    this.items.add(room.getHabboItem(settings.getFurniIds()[i]));
                }

                this.setDelay(settings.getDelay());
                return true;
            }
        }
    }

    private int getNewRotation(HabboItem item) {
        int rotationToAdd = 0; // fixado por victor (fanta)
        if (item.getMaximumRotations() == 2) {
            return item.getRotation() == 0 ? 4 : 0;
        } else if (item.getMaximumRotations() == 1) {
            return item.getRotation();
        } else {
            THashSet possibleRotations;
            int index;
            TObjectHashIterator iter;
            int i;
            if (item.getMaximumRotations() > 4) {
                if (this.rotation == 1) {
                    return item.getRotation() == item.getMaximumRotations() - 1 ? 0 : item.getRotation() + 1;
                }

                if (this.rotation == 2) {
                    return item.getRotation() > 0 ? item.getRotation() - 1 : item.getMaximumRotations() - 1;
                }

                if (this.rotation == 3) {
                    possibleRotations = new THashSet();

                    for(index = 0; index < item.getMaximumRotations(); ++index) {
                        possibleRotations.add(index);
                    }

                    possibleRotations.remove(item.getRotation());
                    if (possibleRotations.size() > 0) {
                        index = Emulator.getRandom().nextInt(possibleRotations.size());
                        iter = possibleRotations.iterator();

                        for(i = 0; i < index; ++i) {
                            iter.next();
                        }

                        return (Integer)iter.next();
                    }
                }
            } else {
                if (this.rotation == 1) {
                    return (item.getRotation() + 2) % 8;
                }

                if (this.rotation == 2) {
                    int rot = (item.getRotation() - 2) % 8;
                    if (rot < 0) {
                        rot += 8;
                    }

                    return rot;
                }

                if (this.rotation == 3) {
                    possibleRotations = new THashSet();

                    for(index = 0; index < item.getMaximumRotations(); ++index) {
                        possibleRotations.add(index * 2);
                    }

                    possibleRotations.remove(item.getRotation());
                    if (possibleRotations.size() > 0) {
                        index = Emulator.getRandom().nextInt(possibleRotations.size());
                        iter = possibleRotations.iterator();

                        for(i = 0; i < index; ++i) {
                            iter.next();
                        }

                        return (Integer)iter.next();
                    }
                }
            }

            return item.getRotation();
        }
    }

    private RoomUserRotation getMovementDirection() {
        RoomUserRotation movemementDirection = RoomUserRotation.NORTH;
        if (this.direction == 1) {
            movemementDirection = RoomUserRotation.values()[Emulator.getRandom().nextInt(RoomUserRotation.values().length / 2) * 2];
        } else if (this.direction == 2) {
            if (Emulator.getRandom().nextInt(2) == 1) {
                movemementDirection = RoomUserRotation.EAST;
            } else {
                movemementDirection = RoomUserRotation.WEST;
            }
        } else if (this.direction == 3) {
            if (Emulator.getRandom().nextInt(2) != 1) {
                movemementDirection = RoomUserRotation.SOUTH;
            }
        } else if (this.direction == 4) {
            movemementDirection = RoomUserRotation.SOUTH;
        } else if (this.direction == 5) {
            movemementDirection = RoomUserRotation.EAST;
        } else if (this.direction == 7) {
            movemementDirection = RoomUserRotation.WEST;
        }

        return movemementDirection;
    }

    public void cycle(Room room) {
        this.itemCooldowns.clear();
    }

    static {
        type = WiredEffectType.MOVE_ROTATE;
    }

    static class JsonData {
        int direction;
        int rotation;
        int delay;
        List<Integer> itemIds;

        public JsonData(int direction, int rotation, int delay, List<Integer> itemIds) {
            this.direction = direction;
            this.rotation = rotation;
            this.delay = delay;
            this.itemIds = itemIds;
        }
    }
}
