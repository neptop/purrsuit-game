package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;

public class Door {
    private Cell cell;
    private final char id; // Unique identifier for the door, it's matched by a lowercase switch such that switch 'a' opens door 'A'
    private boolean isOpen;

    public Door(Cell cell, char id, boolean isOpen) {
        this.cell = cell;
        this.id = id;
        this.isOpen = isOpen;
    }

    public Cell getCell() {
        return cell;
    }
    public char getId() {
        return id;
    }
    public boolean isOpen() {
        return isOpen;
    }
    public void setOpen(boolean open) {
        isOpen = open;
    }
    public void toggle() {
        isOpen = !isOpen;
    }

}
