package com.purrsuit.game.util;

public final class GameConfig {
    private GameConfig() {}
    public static final int VIEW_W_TILES = 28; //
    public static final int VIEW_H_TILES = 31; //
    public static final float PPM = 16f; // pixels per meter (for Box2D scaling)
    public static final float WORLD_WIDTH = VIEW_W_TILES;
    public static final float WORLD_HEIGHT = VIEW_H_TILES;
}
