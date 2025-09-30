package com.purrsuit.game.ecs;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;

public class Player {
    private final WorldGrid grid;

    // movement configs
    private static final float TILE_SPEED = 6.0f; // tiles per second
    private static final float CORNER_RADIUS = 0.40f; // tiles, buffer radius
    private static final float RECENTER_LERP = 0.15f; // how quickly to recenter in tile

    // state
    private Cell currentCell;
    private Cell targetCell;
    private Direction dir;
    private Direction targetDir = null;
    private float x,y; // position in tile cords
    private float t = 0f; // interpolation factor 0..1

    // utilities
    private float centerX(Cell c) { return c.x() + 0.5f; }
    private float centerY(Cell c) { return c.y() + 0.5f; }

    public Player(WorldGrid grid, Cell startCell, Direction startDir) {
        this.grid = grid;
        this.currentCell = startCell;
        this.targetCell = null;
        this.targetDir = null;
        this.dir = startDir;
        this.x = centerX(startCell);
        this.y = centerY(startCell);
    }

    private void readInput() {
        Direction lastDir = null;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {
            lastDir = Direction.LEFT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {
            lastDir = Direction.RIGHT;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {
            lastDir = Direction.UP;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {
            lastDir = Direction.DOWN;
        }

        // no 180 turns
        if (lastDir != null) {
            if(!(targetCell != null && lastDir == dir.opposite())) {
                targetDir = lastDir;
            }
        }
    }

    private void attemptTurn() {
        if (targetDir == null){
            return;
        }
        if (targetCell != null && targetDir == dir.opposite()) {
            return;
        }

        Cell nextCell = currentCell.next(targetDir);
        if (grid.passable(nextCell)) {
            targetCell = nextCell;
            dir = targetDir;
            t = 0f;
            targetDir = null;
        }
    }

    public boolean nearCell(Cell c) {
        float dx = Math.abs(x - centerX(c));
        float dy = Math.abs(y - centerY(c));
        return Math.max(dx, dy) < CORNER_RADIUS;
    }

    private void recenterToCorridor(){
        if (dir == null) {
            float cy = centerY(currentCell);
            float cx = centerX(currentCell);
            x = MathUtils.lerp(x, cx, RECENTER_LERP);
            y = MathUtils.lerp(y, cy, RECENTER_LERP);
            return;
        }
        float targetX;
        float targetY;
        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            targetY = centerY(currentCell);
            y = MathUtils.lerp(y, targetY, RECENTER_LERP);
        } else {
            targetX = centerX(currentCell);
            x = MathUtils.lerp(x, targetX, RECENTER_LERP);
        }
    }

    public void render(ShapeRenderer shapes) {
        shapes.setColor(new Color(0.2f, 0.8f, 0.9f, 1f));
        shapes.circle(x, y, 0.38f, 20);
        shapes.rectLine(x, y, x + 0.35f * dir.dx, y + 0.35f * dir.dy, 0.06f);
    }

    public Cell getCurrentCell() {
        return currentCell;
    }
    public Direction getDirection() {
        return dir;
    }
    public float getX() {
        return x;
    }
    public float getY() {
        return y;
    }


    // public API
    public void update(float dt) {
        readInput();

        // corner buffering
        if (nearCell(currentCell)){
            attemptTurn();
        }

        // if no target, try to move forward if possible
        if(targetCell == null){
            Cell nextCell = currentCell.next(dir);
            if (grid.passable(nextCell)) {
                targetCell = nextCell;
                t = 0f;
            }
        }

        // move towards next cell center
        if (targetCell != null) {
            t += dt * TILE_SPEED;
            if (t >= 1f) {
                // reached target cell
                currentCell = targetCell;
                x = centerX(currentCell);
                y = centerY(currentCell);
                targetCell = null;
                t = 0f;

                attemptTurn();
                Cell forwardCell = currentCell.next(dir);
                if(grid.passable(forwardCell)){
                    targetCell = forwardCell;
                } else {
                    dir = null; // stop movement
                }

            } else {
                // interpolate towards target cell
                x = MathUtils.lerp(centerX(currentCell), centerX(targetCell), t);
                y = MathUtils.lerp(centerY(currentCell), centerY(targetCell), t);
            }
        } else {
            // no movement, recenter in tile
            recenterToCorridor();
        }
    }
}
