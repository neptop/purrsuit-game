package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;

// computes a low-cost map to the cheese but also adds heat around the player so the mice pathfind around the player
public class HeatField {
    private final float[][] heat;
    private final int width;
    private final int height;

    private static final float CENTER_BOOST = 2_000f; // player cell
    private static final float RING1_MULTIPLIER = 12f; // immediate neighbors get higher heat
    private static final float AHEAD_BASE_PENALTY = 250f; // penalty in facing direction
    private static final float AHEAD_RADIUS = 8; // how far ahead to apply penalty
    private static final float FALLOFF_EXP = 3.0f; // how quickly penalty falls off

    public HeatField(int width, int height) {
        this.width = width;
        this.height = height;
        this.heat = new float[width][height];
    }

    public void computeHeatMap(Cell player, Direction dir, int radius, float peakPenalty){
        // reset heat map
        for (int x = 0; x <width; x++) {
            for (int y = 0; y < height; y++) {
                heat[x][y] = 0f;
            }
        }
        // add heat in a radius around the player
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                int cx = player.x() + dx;
                int cy = player.y() + dy;
                if (cx < 0 || cx >= width || cy < 0 || cy >= height) continue; // out of bounds
                int dist = Math.abs(dx) + Math.abs(dy);
                if (dist > radius) continue; // outside radius
                if (dist == 0) {
                    heat[cx][cy] += CENTER_BOOST; // center cell
                    continue;
                }

                float t = (radius - dist) / (float) radius; // 1.0 at center, 0.0 at edge
                float base = (float) Math.pow(t, FALLOFF_EXP); // falloff
                if(dist == 1){
                    base *= RING1_MULTIPLIER; // immediate neighbors get higher heat
                }
                heat[cx][cy] += base;
            }
        }

        // slight bias in the direction the player is facing to encourage mice to go around the player
        if (dir != null) {
            int fx = player.x() + dir.dx;
            int fy = player.y() + dir.dy;
            for (int i = 0; i < radius; i++){
                int cx = fx + dir.dx * i;
                int cy = fy + dir.dy * i;
                if (cx < 0 || cx >= width || cy < 0 || cy >= height) break; // out of bounds
                float factor = (AHEAD_RADIUS - i) / (float) AHEAD_RADIUS; // 1.0 at player cell, 0.0 at max radius
                heat[cx][cy] += AHEAD_BASE_PENALTY * factor;
            }
        }
    }
    public float getHeat(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return Float.POSITIVE_INFINITY; // out of bounds
        }
        return heat[x][y];
    }
}
