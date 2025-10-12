package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;

public interface CellBlocker {
    boolean isBlocked(Cell c);
}
