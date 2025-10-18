package com.purrsuit.game.ecs;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.purrsuit.game.util.Cell;
import java.util.ArrayDeque;
import java.util.Deque;

public class TetheredCheese {
    // fields
    private final Deque<Cell> trail = new ArrayDeque<>();
    private final int tetherLength; // distance behind player (3 cells atm)
    private int maxHp = 3;
    private int currentHp = maxHp;

    // getters
    public int getMaxHp() { return maxHp; }
    public int getCurrentHp() { return currentHp; }

    // methods
    public boolean isNotActive() { return currentHp <= 0; }
    public void damage (int amount) { currentHp = Math.max(0, currentHp - amount); }
    public void heal (int amount) { currentHp = Math.min(maxHp, currentHp + amount); }


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

    // temp simple rendering
    public void render(ShapeRenderer shapes) {
        shapes.setColor(Color.YELLOW);
        Cell prev = null;
        for (Cell c : trail) {
            if (prev != null) {
                float x1 = prev.x() + 0.5f;
                float y1 = prev.y() + 0.5f;
                float x2 = c.x() + 0.5f;
                float y2 = c.y() + 0.5f;
                shapes.rectLine(x1, y1, x2, y2, 0.1f);
            }
            prev = c;
        }
        Cell cheese = getCheeseCell();
        if (cheese != null) {
            shapes.circle(cheese.x() + 0.5f, cheese.y() + 0.5f, 0.3f, 20);
        }
    }
}
