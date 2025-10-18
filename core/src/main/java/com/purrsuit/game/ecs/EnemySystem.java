package com.purrsuit.game.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EnemySystem {
    private final WorldGrid grid;
    private final Pathfinder path;
    private final HeatField heat;
    private final List<EnemyChaserMice> enemies = new ArrayList<>();

    public EnemySystem(WorldGrid grid) {
        this.grid = grid;
        this.path = new Pathfinder(grid);
        this.heat = new HeatField(grid.getWidth(), grid.getHeight());
    }

    public boolean hasEnemyAt(Cell c) {
        for(EnemyChaserMice e : enemies){
            if(e.getCurrentCell().equals(c)) return true;
        }
        return false;
    }

    public void killEnemiesAt(Cell c) {
        Iterator<EnemyChaserMice> iter = enemies.iterator();
        while (iter.hasNext()) {
            if (iter.next().getCurrentCell().equals(c)) {
                iter.remove();
            }
        }
    }

    // spawn an enemy at cell c if passable
    public void spawn(Cell c) {
        if(!grid.passable(c)) return;
        for(EnemyChaserMice e : enemies){
            if(e.getCurrentCell().equals(c)) return; // already an enemy here
        }
        enemies.add(new EnemyChaserMice(c));
    }
    public List<EnemyChaserMice> all() {return enemies;}

    public void update(float delta, Cell cheese, Cell playerCell,Direction playerDir, TetheredCheese tether) {
        // update heat field
        heat.computeHeatMap(playerCell, playerDir, 4, 2.0f);

        // dijkstra from cheese with heat penalties
        path.computeDist(cheese, heat);


        // reserve cells to prevent enemies from colliding
        java.util.Set<Cell> reserved = new java.util.HashSet<>();
        for (EnemyChaserMice e : enemies) reserved.add(e.getCurrentCell());
        for (EnemyChaserMice e : enemies) {
            reserved.add(e.getTargetCell());
            Cell target = e.getTargetCell();
            if (target != null) {
                reserved.add(target);
            }
        }

        // try beginstep for each enemy
        for (EnemyChaserMice e : enemies) {
            if(e.getTargetCell() == null){
                if(e.tryBeginStep(path, reserved)){
                    Cell target = e.getTargetCell();
                    if (target != null) {
                        reserved.add(target);
                    }
                }
            }
        }

        // update enemies pos
        for (EnemyChaserMice e : enemies) {
            e.update(delta, path);
        }

        // collisions
        Iterator<EnemyChaserMice> iter = enemies.iterator();
        while (iter.hasNext()) {
            EnemyChaserMice e = iter.next();

            if (e.getCurrentCell().equals(playerCell)) {
                iter.remove();
                continue;
            }

            // enemy reaches cheese -> cheese takes damage and enemy removed
            if (e.getCurrentCell().equals(cheese)) {
                tether.damage(1);
                iter.remove();
            }
        }
    }
    public void render(ShapeRenderer shapes) {
        for (EnemyChaserMice e : enemies) {
            e.render(shapes);
        }
    }
}
