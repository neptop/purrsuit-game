package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;
import java.util.*;

public class Level {
    private final WorldGrid grid;
    private final Cell start;
    private final Cell exit;
    private final List<Cell> spawners;
    private final Map<Character, List<Cell>> doorsById;
    private final Map<Character, List<Cell>> switchesById;
    private final List<Cell> coins;

    public Level(WorldGrid grid, Cell start, Cell exit, List<Cell> spawners,
                 Map<Character, List<Cell>> doorsById,
                 Map<Character, List<Cell>> switchesById,
                 List<Cell> coins) {
        this.grid = grid;
        this.start = start;
        this.exit = exit;
        this.spawners = spawners != null ? spawners : new ArrayList<Cell>();
        this.doorsById = doorsById != null ? doorsById : new HashMap<Character, List<Cell>>();
        this.switchesById = switchesById != null ? switchesById : new HashMap<Character, List<Cell>>();
        this.coins = (coins != null) ? coins : new ArrayList<Cell>();
    }

    public WorldGrid getGrid() {
        return grid;
    }
    public Cell getStart() {
        return start;
    }
    public Cell getExit() {
        return exit;
    }
    public Map<Character, List<Cell>> getDoorsById() {return doorsById;}
    public Map<Character, List<Cell>> getSwitchesById() {return switchesById;}
    public List<Cell> getSpawners() { return spawners; }
    public List<Cell> getCoins() { return coins; }

    public static Level ASCIIToLevel(String[] rows) {
        // row[0] is top row
        int height = rows.length;
        int width = rows[0].length();
        WorldGrid grid = new WorldGrid(width, height);

        Cell start = null;
        Cell exit = null;
        List<Cell> spawners = new ArrayList<Cell>();
        Map<Character, List<Cell>> doorsById = new HashMap<Character, List<Cell>>();
        Map<Character, List<Cell>> switchesById = new HashMap<Character, List<Cell>>();
        List<Cell> coins = new ArrayList<Cell>();

        for (int ry = 0; ry < height; ry++) {
            String row = rows[ry];
            if (row.length() != width) {
                throw new IllegalArgumentException("Row width mismatch at y= " + ry);
            }
            int y = height - 1 - ry; // invert y to have (0,0) at bottom-left

            for (int x = 0; x < width; x++) {
                char c = row.charAt(x);

                // set walls
                if (c =='#') {
                    grid.setWall(x, y, true);
                    continue;
                }
                // set everything else as walkable
                grid.setWall(x, y, false);

                if (c == 'S') { // set start
                    start = new Cell(x, y);
                } else if (c == 'E') { // set exit
                    exit = new Cell(x, y);
                } else if (c == 'M') { // enemy spawner
                    spawners.add(new Cell(x, y));
                } else if (c >= 'A' && c <= 'Z') { // door
                    char idLower = Character.toLowerCase(c);
                    Cell doorCell = new Cell(x, y);
                    if (!doorsById.containsKey(idLower)) {
                        doorsById.put(idLower, new ArrayList<Cell>());
                    }
                    doorsById.get(idLower).add(doorCell);
                } else if (c >= 'a' && c <= 'z') { // switch
                    char idLower = c;
                    Cell switchCell = new Cell(x, y);
                    if (!switchesById.containsKey(idLower)) {
                        switchesById.put(idLower, new ArrayList<Cell>());
                    }
                    switchesById.get(idLower).add(switchCell);
                } else if (c == '$') { // coin
                    coins.add(new Cell(x, y));
                }
                // else '.' or any other chari is treated as floor
            }
        }
        if (start == null) {
            throw new IllegalArgumentException("Level has no 'S' Start");
        }
        if (exit == null) {
            throw new IllegalArgumentException("Level has no 'E' Exit");
        }
        return new Level(grid, start, exit, spawners, doorsById, switchesById, coins);
    }
}
