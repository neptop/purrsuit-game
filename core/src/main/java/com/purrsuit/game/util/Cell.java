package com.purrsuit.game.util;

public record Cell(int x, int y) {
    public Cell next(Direction d) {
        return new Cell(x + d.dx, y + d.dy);
    }
}
