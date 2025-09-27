package com.purrsuit.game.screens;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.purrsuit.game.ecs.WorldGrid;
import com.purrsuit.game.util.GameConfig;

public class PlayScreen extends ScreenAdapter {
    private final OrthographicCamera cam = new OrthographicCamera();
    private final FitViewport viewport = new FitViewport(GameConfig.WORLD_WIDTH, GameConfig.WORLD_HEIGHT, cam); // 1 unit = 1 tile
    private final ShapeRenderer shapes = new ShapeRenderer();

    private  WorldGrid grid;

    @Override
    public void show() {
        cam.position.set(GameConfig.WORLD_WIDTH*0.5f, GameConfig.WORLD_HEIGHT*0.5f, 0f);
        cam.update();
        grid = new WorldGrid(GameConfig.VIEW_W_TILES, GameConfig.VIEW_H_TILES);

        // temp walls
        grid.setWall(GameConfig.VIEW_W_TILES/2, GameConfig.VIEW_H_TILES/2, true);
    }

    @Override
    public void render (float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f); // dark blueish
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // clear the screen
        cam.update();
        shapes.setProjectionMatrix(cam.combined);

        // draw grid
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(new Color(1f,1f,1f,0.12f)); // light white
        for (int x = 0; x <= grid.getWidth(); x++) {
            for (int y = 0; y <= grid.getHeight(); y++) {
                shapes.rect(x,y,1,1);
            }
        }
        shapes.end();

        // draw walls
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(new Color(0.8f,0.2f,0.2f,0.8f)); // semi-transparent red
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                if (grid.isWall(new com.purrsuit.game.util.Cell(x,y))) {
                    shapes.rect(x,y,1,1);
                }
            }
        }
        shapes.end();
    }

    @Override
    public void resize (int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose () {
        shapes.dispose();
    }
}
