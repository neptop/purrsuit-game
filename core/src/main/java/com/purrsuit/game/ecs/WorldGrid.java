package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;

public class WorldGrid {
    private final int width;
    private final int height;
    private final boolean [][] wall; // true if wall, false if walkable

    public WorldGrid(int width, int height) {
        this.width = width;
        this.height = height;
        this.wall = new boolean[width][height];

        // temp
        for (int x = 0; x < width; x++) {
            wall[x][0] = true;
            wall[x][height - 1] = true;
        }
        for (int y = 0; y < height; y++) {
            wall[0][y] = true;
            wall[width - 1][y] = true;
        }
    }

    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }

    public boolean inBounds(Cell c) {
        return c.x() >= 0 && c.x() < width && c.y() >= 0 && c.y() < height; // true if in bounds
    }

    public boolean isWall(Cell c) {
        return !inBounds(c) || wall[c.x()][c.y()]; // true if out of bounds or a wall
    }

    public boolean passable(Cell c) {
        return inBounds(c) && !wall[c.x()][c.y()]; // true if in bounds and not a wall
    }

    // temp method to set walls
    public void setWall(int x, int y, boolean v) {
        wall[x][y] = v;
    }
}
