package com.purrsuit.game.ecs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.purrsuit.game.util.Cell;
import java.util.ArrayDeque;
import java.util.Deque;

public class TetheredCheese {
    // fields
    private final Deque<Cell> trail = new ArrayDeque<>();
    private final int tetherLength; // distance behind player (3 cells atm)
    private int maxHp = 3;
    private int currentHp = maxHp;
    private boolean invulnerable = false;

    // getters
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }
    public boolean isInvulnerable() { return invulnerable; }

    // methods
    public boolean isNotActive() { return currentHp <= 0; }
    public void damage (int amount) { if (invulnerable) return; currentHp = Math.max(0, currentHp - amount); }
    public void heal (int amount) { currentHp = Math.min(maxHp, currentHp + amount); }
    public void setInvulnerable(boolean invulnerable) { this.invulnerable = invulnerable; }

    public TetheredCheese(Cell start, int tetherLength) {
        this.tetherLength = Math.max(1, tetherLength);
        trail.addFirst(start);
    }

    // Call this every time the player enters a new cell center
    public void onHeadMoved(Cell newHead) {
        // don't allow duplicate push if it's the same as current head
        Cell current = trail.peekFirst();
        if (current == null || current.x() != newHead.x() || current.y() != newHead.y()) {
            trail.addFirst(newHead);
            // maintain tether length
            while (trail.size() > tetherLength + 1) { // +1 to account for head cell
                trail.removeLast();
            }
        }
    }

    // return current cheese cell
    public Cell getCheeseCell() {
        int index = tetherLength;
        if (trail.isEmpty()) return null;
        int i = 0;
        for (Cell c : trail) {
            if (i == index) return c;
            i++;
        }
        return trail.peekLast(); // if not enough length, return last cell
    }

    public boolean occupiesTrail(Cell c){
        if (trail.isEmpty()) return false;
        Cell head = trail.peekFirst();
        for (Cell t: trail) {
            if (t.equals(head)) continue; // skip head cell
            if (t.equals(c)) return true;
        }
        return false;
    }

    // treat the cheese cell as a solid object for collision purposes
    public boolean blocks(Cell c) {
        Cell cheeseCell = getCheeseCell();
        return cheeseCell != null && cheeseCell.equals(c);
    }

    public void render(SpriteBatch batch, TextureAtlas.AtlasRegion tetherTex) {
        Cell prev = null;
        for (Cell c : trail) {
            if (prev != null) {
                drawSegment(batch, prev, c, tetherTex);
            }
            prev = c;
        }
    }

    private void drawSegment(SpriteBatch batch, Cell a, Cell b, TextureAtlas.AtlasRegion texture) {
        float x1 = a.x() + 0.5f;
        float y1 = a.y() + 0.5f;
        float x2 = b.x() + 0.5f;
        float y2 = b.y() + 0.5f;

        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float)Math.sqrt(dx * dx + dy * dy);
        float angleDeg = (float)(Math.atan2(dy, dx) * 180f / Math.PI);

        float thickness = 1f; // thickness of the tether segment

        batch.draw(
            texture,
            x1 - thickness / 2f, // center the texture on the line
            y1 - thickness / 2f,
            thickness / 2f, // origin at center of thickness
            thickness / 2f,
            len,
            thickness,
            1f,
            1f,
            angleDeg
        );
    }
}
