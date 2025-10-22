package com.purrsuit.game.ecs;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.purrsuit.game.util.Cell;

public class PowerUpPickup {
    private final PowerUpType type;
    private final Cell cell;
    private float ttl; // time till despawn, in seconds

    public PowerUpPickup(PowerUpType type, Cell cell, float ttl) {
        this.type = type;
        this.cell = cell;
        this.ttl = ttl;
    }

    public PowerUpType getType() {
        return type;
    }

    public Cell getCell() {
        return cell;
    }

    public boolean isAlive() {
        return ttl > 0f;
    }

    public void update(float delta) {
        ttl -= delta;
    }

    public void render(SpriteBatch batch, TextureAtlas.AtlasRegion tex) {
        batch.draw(
            tex,
            cell.x(),
            cell.y(),
            1f,
            1f
        );
    }
}
