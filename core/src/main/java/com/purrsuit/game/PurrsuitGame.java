package com.purrsuit.game;

import com.badlogic.gdx.Game;

public class PurrsuitGame extends Game {
    @Override
    public void create() {
        setScreen(new PlayScreen());
    }
}
