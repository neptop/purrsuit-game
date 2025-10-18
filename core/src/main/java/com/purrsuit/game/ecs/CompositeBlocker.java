package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;

public class CompositeBlocker implements CellBlocker{
    private final CellBlocker[] blockers;

    public CompositeBlocker(CellBlocker...blockers) {
        this.blockers = blockers;
    }

    @Override
    public boolean isBlocked(Cell cell) {
        if (blockers == null) {
            return false;
        }
        for (CellBlocker blocker : blockers) {
            if (blocker != null && blocker.isBlocked(cell)) {
                return true;
            }
        }
        return false;
    }
}
