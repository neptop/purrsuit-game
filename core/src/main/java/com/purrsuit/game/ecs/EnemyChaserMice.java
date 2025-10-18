package com.purrsuit.game.ecs;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.graphics.Color;

public class EnemyChaserMice {
    private static final float SPEED = 4.0f; // tiles per sec. Slower than player

    private Cell currentCell;
    private Cell targetCell;
    private Direction dir;
    private float x, y;
    private float t = 0f; // interpolation factor 0..1

    public EnemyChaserMice(Cell spawn) {
        this.currentCell = spawn;
        this.targetCell = null;
        this.x = spawn.x() + 0.5f;
        this.y = spawn.y() + 0.5f;
    }

    public Cell getCurrentCell() { return currentCell; }
    public Cell getTargetCell() { return targetCell; }
    public Direction getDirection() { return dir; }

    // attempt to begin a step towards the best next cell according to the pathfinder
    // this should prevent 2 enemies from moving into the same cell
    public boolean tryBeginStep(Pathfinder pf, java.util.Set<Cell> reserved){
        if (targetCell != null) return true; // already moving
        Direction nextDir = pf.bestStep(currentCell);
        if (nextDir == null) return false; // no valid step
        Cell nextCell = currentCell.next(nextDir);
        if (reserved.contains(nextCell)) return false; // cell is reserved
        dir = nextDir;
        targetCell = nextCell;
        t = 0f;
        return true;
    }

    public void update(float delta, Pathfinder pf){
        if (targetCell == null) {
            Direction nextDir = pf.bestStep(currentCell);
            if (nextDir != null) {
                dir = nextDir;
                targetCell = currentCell.next(dir);
                t = 0f;
            }
        }
        t += delta * SPEED;
        if (t >= 1f){
            currentCell = targetCell;
            targetCell = null;
            t = 0f;
            x = currentCell.x() + 0.5f;
            y = currentCell.y() + 0.5f;
        } else {
            x = MathUtils.lerp(currentCell.x() + 0.5f, targetCell.x() + 0.5f, t);
            y = MathUtils.lerp(currentCell.y() + 0.5f, targetCell.y() + 0.5f, t);
        }
    }
    public void render(ShapeRenderer shapes){
        shapes.setColor((new Color(0.95f, 0.5f, 0.2f, 1f))); // orange
        shapes.circle(x, y, 0.35f, 18);
    }
}
