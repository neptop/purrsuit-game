package com.purrsuit.game;

import com.badlogic.gdx.Game;
import io.github.some_example_name.screens.PlayScreen;

public class PurrsuitGame extends Game {
    @Override
    public void create() {
        setScreen(new PlayScreen(this));
    }
}
