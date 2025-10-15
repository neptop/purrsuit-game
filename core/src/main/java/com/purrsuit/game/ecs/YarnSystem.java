package com.purrsuit.game.ecs;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class YarnSystem {
    public interface ImpactHook{
        void onImpact(Cell impactCell, Direction dir);
    }

    private final WorldGrid grid;
    private final List<YarnProjectile> projectiles = new ArrayList<>();
    private ImpactHook hook;

    public YarnSystem(WorldGrid grid) {
        this.grid = grid;
    }

    public void setImpactHook(ImpactHook hook) {
        this.hook = hook;
    }
    public void shoot(Cell startCell, Direction dir) {
        YarnProjectile p = new YarnProjectile(grid, startCell, dir, (impactCell, impactDir) -> {
            if (hook != null) {
                try {
                    hook.onImpact(impactCell, impactDir);
                } catch (Exception ignore) {
                }
            }
        });
        projectiles.add(p);
    }

    public void update(float dt) {
        Iterator<YarnProjectile> iter = projectiles.iterator();
        while (iter.hasNext()) {
            YarnProjectile p = iter.next();
            boolean active = p.update(dt);
            if (!active) {
                iter.remove();
            }
        }
    }
    public void render(ShapeRenderer shapes) {
        for (YarnProjectile p : projectiles) {
            p.render(shapes);
        }
    }
}
