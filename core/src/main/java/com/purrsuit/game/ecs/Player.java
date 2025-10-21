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
    private final CellBlocker blocker;
    private final StepListener stepListener;

    // movement configs
    private static final float TILE_SPEED = 6.0f; // tiles per second
    private static final float CORNER_RADIUS = 0.40f; // tiles, buffer radius
    private static final float RECENTER_LERP = 0.15f; // how quickly to recenter in tile

    // state
    private Cell currentCell;
    private Cell targetCell;
    private Direction dir;
    private Direction bufferedTurn;
    private float x,y; // position in tile cords
    private float t = 0f; // interpolation factor 0..1
    private final PowerUpSystem powerups;

    // utilities
    private float centerX(Cell c) { return c.x() + 0.5f; }
    private float centerY(Cell c) { return c.y() + 0.5f; }

    // getters
    public Cell getCurrentCell() { return currentCell; }
    public Direction getDirection() { return dir; }
    public float getRenderX() { return x; }
    public float getRenderY() { return y; }

    public Player(WorldGrid grid, Cell startCell, Direction startDir, CellBlocker blocker, StepListener stepListener, PowerUpSystem powerups) {
        this.grid = grid;
        this.currentCell = startCell;
        this.targetCell = null;
        this.bufferedTurn = null;
        this.dir = startDir;
        this.x = centerX(startCell);
        this.y = centerY(startCell);
        this.blocker = blocker;
        this.stepListener = stepListener;
        this.powerups = powerups;
    }

    private boolean passable(Cell c){
        return grid.passable(c) && (blocker == null || !blocker.isBlocked(c));
    }

    private void readInput() {
        Direction lastDir = null;
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) {lastDir = Direction.LEFT;}
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) {lastDir = Direction.RIGHT;}
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) {lastDir = Direction.UP;}
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) {lastDir = Direction.DOWN;}

        bufferedTurn = lastDir;
    }

    // decide movement when at the center of a cell
    private void decideCenter() {
        if (bufferedTurn != null) {
            Cell cand = currentCell.next(bufferedTurn);
            if (passable(cand)) {
                dir = bufferedTurn;
                targetCell = cand;
                t = 0f;
                bufferedTurn = null;
                return;
            }
        }
        if (isPressed(dir)) {
            Cell fwd = currentCell.next(dir);
            if (passable(fwd)) {
                targetCell = fwd;
                t = 0f;
            }
        }
    }

    private void recenterToCorridor(){
        if (dir == Direction.LEFT || dir == Direction.RIGHT) {
            float targetY = centerY(currentCell);
            y = MathUtils.lerp(y, targetY, RECENTER_LERP);
        } else if (dir == Direction.UP || dir == Direction.DOWN) {
            float targetX = centerX(currentCell);
            x = MathUtils.lerp(x, targetX, RECENTER_LERP);
        }
        x = MathUtils.lerp(x, centerX(currentCell), RECENTER_LERP * 0.35f);
        y = MathUtils.lerp(y, centerY(currentCell), RECENTER_LERP * 0.35f);
    }

    private boolean isPressed(Direction d) {
        switch (d) {
            case LEFT:
                return Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
            case RIGHT:
                return Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
            case UP:
                return Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
            case DOWN:
                return Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
            default:
                return false;
        }
    }

    public void render(ShapeRenderer shapes) {
        shapes.setColor(new Color(0.2f, 0.8f, 0.9f, 1f));
        shapes.circle(x, y, 0.38f, 20);
        shapes.rectLine(x, y, x + 0.35f * dir.dx, y + 0.35f * dir.dy, 0.06f);
    }

    // public API
    public void update(float dt) {
        readInput();

        // move when key is held
        if(targetCell == null) {
            decideCenter();
        }

        // move towards next cell center
        if (targetCell != null) {
            float speed = TILE_SPEED * (powerups != null ? powerups.speedMultiplier() : 1f);
            t += dt * speed;
            if (t >= 1f) {
                // reached target cell
                currentCell = targetCell;
                x = centerX(currentCell);
                y = centerY(currentCell);
                targetCell = null;
                t = 0f;

                if (stepListener != null) stepListener.onEnter(currentCell);

                decideCenter();

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
