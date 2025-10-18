package com.purrsuit.game.ecs;

import com.purrsuit.game.util.Cell;

public class EnemySpawner {
    private final Cell cell;
    private final float spawnInterval;
    private float timer;

    public EnemySpawner(Cell cell, float spawnInterval) {
        this.cell = cell;
        this.spawnInterval = spawnInterval;
        this.timer = 0f;
    }

    public Cell getCell() {return cell;}

    public boolean tick(float delta) {
        timer += delta;
        if (timer >= spawnInterval) {
            timer -= spawnInterval;
            return true;
        }
        return false;
    }
}
