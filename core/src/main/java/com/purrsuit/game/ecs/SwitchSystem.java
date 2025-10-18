package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;
import java.util.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class SwitchSystem {
    private final Map<Character, List<Switch>> switchesById = new HashMap<>();
    private final Map<Cell, Switch> switchByCell = new HashMap<>();

    public void addSwitch(Switch sw) {
        switchByCell.put(sw.getCell(), sw);
        switchesById.computeIfAbsent(sw.getId(), k -> new ArrayList<>()).add(sw);
    }

    // Returns the switch at the given cell, or null if none exists
    public Switch at (Cell cell) {
        return switchByCell.get(cell);
    }

    // Returns the switch id at the given cell, or null if none exists
    public Character getSwitchIdAt(Cell cell) {
        Switch sw = switchByCell.get(cell);
        if (sw != null) {
            return sw.getId();
        }
        return null;
    }

    // Toggles all switches with the given lowercase id
    public void toggleId(char idLowercase){
        List<Switch> list = switchesById.get(idLowercase);
        if(list == null) return;
        for (Switch s : list) {
            s.toggle();
        }
    }

    public void render(ShapeRenderer shapes) {
        for (Switch s : switchByCell.values()) {
            Cell cell = s.getCell();
            if (s.getState()) {
                shapes.setColor(new Color(1f, 0.9f, 0.3f, 1f)); // on: amberish color
            } else {
                shapes.setColor(new Color(0.9f, 0.9f, 0.9f, 0.9f)); // off: light gray
            }
            shapes.circle(cell.x() + 0.5f, cell.y() + 0.5f, 0.14f, 14);
        }
    }
}
