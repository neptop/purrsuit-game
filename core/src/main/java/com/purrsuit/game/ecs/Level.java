package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;
import java.util.ArrayList;
import java.util.List;

public class Level {
    private final WorldGrid grid;
    private final Cell start;
    private final Cell exit;
    private final List<Cell> spawners;

    public Level(WorldGrid grid, Cell start, Cell exit, List<Cell> spawners) {
        this.grid = grid;
        this.start = start;
        this.exit = exit;
        this.spawners = spawners != null ? spawners : new ArrayList<Cell>();
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
    public List<Cell> getSpawners() { return spawners; }

    public static Level ASCIIToLevel(String[] rows) {
        // row[0] is top row
        int height = rows.length;
        int width = rows[0].length();
        WorldGrid grid = new WorldGrid(width, height);

        Cell start = null;
        Cell exit = null;
        List<Cell> spawners = new ArrayList<Cell>();

        for (int ry = 0; ry < height; ry++) {
            String row = rows[ry];
            if (row.length() != width) {
                throw new IllegalArgumentException("Row width mismatch at y= " + ry);
            }
            int y = height - 1 - ry; // invert y to have (0,0) at bottom-left
            for (int x = 0; x < width; x++) {
                char c = row.charAt(x);
                switch (c) {
                    case '#':
                        grid.setWall(x, y, true);
                        break;
                    case 'S':
                        start = new Cell(x, y);
                        break;
                    case 'E':
                        exit = new Cell(x, y);
                        break;
                    case 'M':
                        // enemy spawner
                        grid.setWall(x,y, false);
                        spawners.add(new Cell(x,y));
                        break;
                    case '.':
                    default:
                        break;
                }
            }
        }
        if (start == null) {
            throw new IllegalArgumentException("Level has no 'S' Start");
        }
        if (exit == null) {
            throw new IllegalArgumentException("Level has no 'E' Exit");
        }
        return new Level(grid, start, exit, spawners);
    }
}
