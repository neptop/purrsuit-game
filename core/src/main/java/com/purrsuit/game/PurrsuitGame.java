package com.purrsuit.game;

import com.badlogic.gdx.Game;
import com.purrsuit.game.screens.MainMenuScreen;

public class PurrsuitGame extends Game {
    @Override
    public void create() {
        setScreen(new MainMenuScreen(this));
    }
}
