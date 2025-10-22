package com.purrsuit.game.ecs;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import com.purrsuit.game.util.Cell;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;

public class CoinSystem {
    private final Set<Cell> coins = new HashSet<>();
    private final Set<Cell> collected = new HashSet<>();

    public CoinSystem(List<Cell> initialCoins) {
        if(initialCoins != null) coins.addAll(initialCoins);
    }

    public boolean hasCoinAt(Cell cell) {
        return coins.contains(cell) && !collected.contains(cell);
    }

    public boolean collect(Cell c){
        if (hasCoinAt(c)) {
            collected.add(c);
            return true;
        }
        return false;
    }

    public int getTotalCoins() {
        return coins.size();
    }
    public int getCollectedCoins() {
        return collected.size();
    }

    public void render(SpriteBatch batch, TextureAtlas.AtlasRegion coinTex) {
        if (coinTex == null) return;
        for (Cell c : coins) {
            if (!collected.contains(c)) {
                batch.draw(coinTex, c.x(), c.y(), 1f, 1f);
            }
        }
    }
}
