package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;

public class Switch {
    private final Cell cell;
    private final char id; // Unique identifier for the switch, it's matched by uppercase so switch 'a' opens door 'A'
    private boolean state; // true = on, false = off

    public Switch(Cell cell, char id, boolean state) {
        this.cell = cell;
        this.id = id;
        this.state = state;
    }

    public Switch(Cell cell, char id) {
        this(cell, id, false);
    }

    public Cell getCell() {
        return cell;
    }
    public char getId() {
        return id;
    }
    public boolean getState() {
        return state;
    }
    public void toggle() {
        state = !state;
    }
}
