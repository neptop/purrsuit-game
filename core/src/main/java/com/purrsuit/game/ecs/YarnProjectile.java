package com.purrsuit.game.ecs;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.Color;
import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;
/**
 A projectile made of yarn that can be thrown in a direction.
 It moves until it hits an enemy or an obstacle.
 When it impacts, it triggers an ImpactListener callback.
 */

public class YarnProjectile {
    public interface ImpactListener {
        void onImpact(Cell impactCell, Direction dir); // tells where the impact happened and in which direction
    }
    public interface CollisionProbe{
        boolean isColliding(Cell c); // returns true if the projectile collides with something in cell c
    }

    private static final float SPEED_TILES_PER_SECOND = 18f;
    private final WorldGrid grid;
    private final ImpactListener listener;
    private final CollisionProbe probe;

    private Cell currentCell;
    private Cell targetCell;
    private Direction dir;
    private float t = 0f; // interpolation factor 0..1
    private boolean active = true;
    private float x, y; // position in tile coords

    public YarnProjectile(WorldGrid grid, Cell startCell, Direction dir, ImpactListener listener, CollisionProbe probe) {
        this.grid = grid;
        this.currentCell = startCell;
        this.targetCell = null;
        this.dir = dir;
        this.listener = listener;
        this.probe = probe;
        this.x = startCell.x() + 0.5f;
        this.y = startCell.y() + 0.5f;
    }
    public boolean isActive() { return active; } // true if still flying
    public Cell getCurrentCell() { return currentCell; }
    public Direction getDirection() { return dir; }

    private boolean passable(Cell c) {
        return grid.passable(c); // only cares about walls here, not entities. Entities are handled by the ImpactListener.
    }

    // returns false if it collides with a wall and is no longer active
    public boolean update(float dt) {
        if (!active) return false;

        if (targetCell == null) {
            Cell nextCell = currentCell.next(dir);

            // wall collision
            if (!passable(nextCell)) { // hit a wall
                active = false;
                if (listener != null) listener.onImpact(currentCell, dir);
                return false;
            }

            // entity collision
            if (probe != null && probe.isColliding(nextCell)) {
                active = false;
                if (listener != null) listener.onImpact(nextCell, dir);
                return false;
            }

            targetCell = nextCell;
            t = 0f;
        }
        t += dt * SPEED_TILES_PER_SECOND;
        if (t >= 1f) {
            // reached target cell
            currentCell = targetCell;
            x = currentCell.x() + 0.5f;
            y = currentCell.y() + 0.5f;
            targetCell = null;
        } else {
            // interpolate position
            x = currentCell.x() + 0.5f + (targetCell.x() - currentCell.x()) * t;
            y = currentCell.y() + 0.5f + (targetCell.y() - currentCell.y()) * t;
        }
        return true;
    }

    // render the yarn projectile as a simple circle for now (light gray color)
    public void render(ShapeRenderer shapes) {
        shapes.setColor(Color.LIGHT_GRAY);
        float tx = x + 0.35f * dir.dx;
        float ty = y + 0.35f * dir.dy;
        shapes.rectLine(x, y, tx, ty, 0.1f);
        shapes.circle(tx, ty, 0.08f, 10);
    }
}
