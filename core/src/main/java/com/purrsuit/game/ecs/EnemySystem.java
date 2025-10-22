package com.purrsuit.game.ecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import java.util.Random;
import java.util.function.Consumer;
import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class EnemySystem {
    private final WorldGrid grid;
    private final Pathfinder path;
    private final HeatField heat;
    private final List<EnemyChaserMice> enemies = new ArrayList<>();
    private final PowerUpSystem powerUps;
    private final float CATNIP_DROP_CHANCE = 0.10f ; // 10% chance to drop catnip on death
    private final Random random = new Random();

    public EnemySystem(WorldGrid grid, CellBlocker blocker, PowerUpSystem powerUps) {
        this.grid = grid;
        this.path = new Pathfinder(grid, blocker);
        this.heat = new HeatField(grid.getWidth(), grid.getHeight());
        this.powerUps = powerUps;
    }

    public boolean hasEnemyAt(Cell c) {
        for(EnemyChaserMice e : enemies){
            if(e.getCurrentCell().equals(c)) return true;
        }
        return false;
    }

    public int killEnemiesAt(Cell c, Consumer<Cell> onEachKilled) {
        int removed = 0;
        Iterator<EnemyChaserMice> iter = enemies.iterator();
        while (iter.hasNext()) {
            EnemyChaserMice e = iter.next();
            if (e.getCurrentCell().equals(c)) {
                iter.remove();
                removed++;
                if (onEachKilled != null){
                    onEachKilled.accept(c); // drops catnip on impact cell
                }
            }
        }
        return removed;
    }


    public boolean rollCatnip(){
        return random.nextFloat() < CATNIP_DROP_CHANCE;
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

        boolean flee = powerUps != null && powerUps.isCatnipActive();

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
                if(e.tryBeginStep(path, reserved, flee)){
                    Cell target = e.getTargetCell();
                    if (target != null) {
                        reserved.add(target);
                    }
                }
            }
        }

        // update enemies pos
        for (EnemyChaserMice e : enemies) {
            e.update(delta, path, flee);
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
