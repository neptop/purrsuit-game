package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;
import java.util.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class DoorSystem implements CellBlocker{
    private final Map<Character, List<Door>> doorsById = new HashMap<>();
    private final Map<Cell, Door> doorsByCell = new HashMap<>();

    public Iterable<Door> allDoors() {
        return doorsByCell.values();
    }

    public void addDoor(Door door) {
        doorsByCell.put(door.getCell(), door);
        doorsById.computeIfAbsent(door.getId(), k -> new ArrayList<>()).add(door);
    }

    // Returns the door at the given cell, or null if none exists
    public void toggle(char idLowercase){
        List<Door> list = doorsById.get(idLowercase);
        if(list == null) return;
        for (Door d : list) {
            d.toggle();
        }
    }

    // Toggles all doors with the given lowercase id
    public boolean toggleDoors(char idLowerccase){
        toggle(idLowerccase);
        return true;
    }


    // Returns true if the cell is blocked by a closed door
    public boolean isBlocked(Cell cell) {
        Door door = doorsByCell.get(cell);
        return door != null && !door.isOpen();
    }

    public void render(ShapeRenderer shapes) {
        for (Door door : doorsByCell.values()) {
            Cell cell = door.getCell();
            if (door.isOpen()) {
                shapes.setColor(new Color(0.25f, 0.25f, 0.25f, 0.01f)); // almost transparent for open doors
            } else {
                shapes.setColor(new Color(0.15f, 0.55f, 0.8f, 0.95f)); // solid blue for closed doors
            }
            shapes.rect(cell.x(), cell.y(), 1f, 1f);
        }
    }
}
