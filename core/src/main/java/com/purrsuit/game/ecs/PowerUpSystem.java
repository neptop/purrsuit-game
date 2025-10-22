package com.purrsuit.game.ecs;

import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.purrsuit.game.util.Cell;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

public class PowerUpSystem {
    public static final float CATNIP_DURATION = 6f; // seconds
    public static final float CATNIP_SPEED_MULTIPLIER = 1.5f; // speed increase to player

    private final List<PowerUpPickup> pickups = new ArrayList<>();
    private float catnipTimer = 0f;

    public void update(float dt){
        if (catnipTimer > 0f){
            catnipTimer -= dt;
            if (catnipTimer < 0f) catnipTimer = 0f;
        }
    }

    public void grant(PowerUpType type){
        if (type == PowerUpType.CATNIP){
            catnipTimer = CATNIP_DURATION;
        }
        // other powerup types can be handled here
    }

    public boolean isCatnipActive() {
        return catnipTimer > 0f;
    }

    public float getCatnipTimeRemaining() {
        return catnipTimer;
    }

    // player speed multiplier hook
    public float speedMultiplier() {
        return isCatnipActive() ? CATNIP_SPEED_MULTIPLIER : 1.0f;
    }
}
