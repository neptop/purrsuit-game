package com.purrsuit.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.purrsuit.game.ecs.WorldGrid;
import com.purrsuit.game.ecs.Player;
import com.purrsuit.game.ecs.Level;
import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;
import com.purrsuit.game.util.GameConfig;

public class PlayScreen extends ScreenAdapter {

    private static final String[] MAP = new String[] {
        "#####################",
        "#S....#...........E.#",
        "#.##..#..#####..###.#",
        "#....##..#...#......#",
        "####.##..#.#.#.###..#",
        "#...... ..#.#.#.....#",
        "#.######.###.#.##...#",
        "#.............#.....#",
        "#####################"
    };

    private OrthographicCamera cam;
    private FitViewport viewport;
    private ShapeRenderer shapes;

    private WorldGrid grid;
    private Player player;
    private Level level;
    private Cell exitCell;
    private boolean win = false;

    @Override
    public void show() {
        // load level from ASCII map
        level = Level.ASCIIToLevel(MAP);
        grid = level.getGrid();
        exitCell = level.getExit();

        // setup camera/viewport
        cam = new OrthographicCamera();
        viewport = new FitViewport(grid.getWidth(), grid.getHeight(), cam);
        cam.position.set(grid.getWidth()*0.5f, grid.getHeight()*0.5f, 0f);
        cam.update();

        shapes = new ShapeRenderer();

        // spawn player at S cell
        player = new Player(grid, level.getStart(), Direction.RIGHT);
    }

    @Override
    public void render (float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f); // dark blueish
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // clear the screen

        if (!win){
            player.update(delta);
            if (player.getCurrentCell().equals(exitCell)){
                win = true;
            }
        }

        cam.update();
        shapes.setProjectionMatrix(cam.combined);

        // draw grid
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(new Color(1f,1f,1f,0.12f)); // light white
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
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
        shapes.setColor(new Color(0.1f, 0.9f, 0.2f, 0.35f)); // start green
        shapes.rect(level.getStart().x(), level.getStart().y(), 1, 1);
        shapes.setColor(new Color(0.1f, 1f, 0.3f, 0.85f)); // exit red
        shapes.rect(exitCell.x(), exitCell.y(), 1, 1);
        shapes.end();

        // draw player
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        player.render(shapes);

        // if you win, draw overlay
        if (win) {
            shapes.setColor(new Color(0f, 0f, 0f, 0.6f));
            shapes.rect(0,0,grid.getWidth(), grid.getHeight());
            shapes.setColor(new Color(1f,0.95f,0.2f,1f));
            shapes.circle(exitCell.x()+0.5f, exitCell.y()+0.5f, 0.5f, 24); // sparkles
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
