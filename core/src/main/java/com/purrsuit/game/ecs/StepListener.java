package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;

public interface StepListener {
    void onEnter(Cell cell);
}
