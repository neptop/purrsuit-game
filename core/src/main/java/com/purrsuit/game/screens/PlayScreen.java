package com.purrsuit.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.purrsuit.game.ecs.*;
import com.purrsuit.game.util.Cell;
import com.purrsuit.game.util.Direction;
import com.purrsuit.game.util.GameConfig;
import com.purrsuit.game.io.LevelIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.purrsuit.game.hud.HUD;

public class PlayScreen extends ScreenAdapter {

    // fields
    private OrthographicCamera cam;
    private FitViewport viewport;
    private ShapeRenderer shapes;
    private WorldGrid grid;
    private Player player;
    private Level level;
    private Cell exitCell;
    private boolean win = false;
    private TetheredCheese tether;
    private Cell lastCell;
    private YarnSystem yarns;
    private boolean canShoot = true;
    private EnemySystem enemies;
    private List<EnemySpawner> spawners;
    private HUD hud;
    private int levelIndex = 1;
    private DoorSystem doorSystem;
    private SwitchSystem switchSystem;

    @Override
    public void show() {
        // load level from ASCII map
        level = LevelIO.load("levels/Level1.txt");
        grid = level.getGrid();
        exitCell = level.getExit();

        // setup camera/viewport
        cam = new OrthographicCamera();
        viewport = new FitViewport(grid.getWidth(), grid.getHeight(), cam);
        cam.position.set(grid.getWidth()*0.5f, grid.getHeight()*0.5f, 0f);
        cam.update();

        shapes = new ShapeRenderer();

        // hud
        hud = new HUD();

        // door and switch systems
        doorSystem = new DoorSystem();
        switchSystem = new SwitchSystem();

        // build from level data
        for (Map.Entry<Character, List<Cell>> doorEntry : level.getDoorsById().entrySet()) {
            char id = doorEntry.getKey();
            for (Cell doorCell : doorEntry.getValue()) {
                doorSystem.addDoor(new Door(doorCell, id, false));
            }
        }
        for (Map.Entry<Character, List<Cell>> switchEntry : level.getSwitchesById().entrySet()) {
            char id = switchEntry.getKey();
            for (Cell switchCell : switchEntry.getValue()) {
                switchSystem.addSwitch(new Switch(switchCell, id));
            }
        }

        // tether length 3 cells behind player
        tether = new TetheredCheese(level.getStart(), 3);

        // composite blocker that includes doors
        CellBlocker compositeBlocker = new CompositeBlocker(
            new CellBlocker(){
                @Override
                public boolean isBlocked(Cell c) {
                    return tether.occupiesTrail(c) || tether.blocks(c);
                }
            },
            doorSystem
        );

        // spawn player pass in composite blocker
        player = new Player(grid, level.getStart(), Direction.RIGHT, compositeBlocker, new StepListener() {
            @Override
            public void onEnter(Cell cell){
                tether.onHeadMoved(cell);
            }
        });

        yarns = new YarnSystem(grid);
        yarns.setBlocker(compositeBlocker);

        enemies = new EnemySystem(grid, doorSystem);
        spawners = new ArrayList<EnemySpawner>();
        for (Cell spawnerCell : level.getSpawners()) {
            spawners.add(new EnemySpawner(spawnerCell, 5f));
        }

        yarns.setCollisionProbe(new YarnSystem.CollisionProbe() {
            @Override
            public boolean isColliding(Cell cell) {
                return enemies.hasEnemyAt(cell);
            }
        });

        yarns.setImpactHook(new YarnSystem.ImpactHook() {
            @Override
            public void onImpact(Cell impactCell, Direction dir) {
                enemies.killEnemiesAt(impactCell);

                Character sid = switchSystem.getSwitchIdAt(impactCell);
                if (sid != null) {
                    boolean opened = doorSystem.toggleDoors(sid.charValue());
                }
            }
        });
    }

    @Override
    public void render (float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f); // dark blueish
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // clear the screen

        if (!win){
            player.update(delta);

            // shoot yarn projectile
            if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
                Direction aim = player.getDirection();
                if (aim != null && canShoot) {
                    yarns.shoot(player.getCurrentCell(), aim);
                }
            }

            yarns.update(delta);

            Cell cheeseCell = tether.getCheeseCell();
            Direction aim = player.getDirection();
            Cell now = player.getCurrentCell();

            for (EnemySpawner spawner : spawners) {
                if (spawner.tick(delta)) {
                    enemies.spawn(spawner.getCell());
                }
            }

            if (cheeseCell != null) {
                enemies.update(delta, cheeseCell, now, aim, tether);
            }

            if (now.equals(exitCell)){
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

        // draw spawners
        shapes.setColor(new Color(0.2f, 0.6f, 1f, 0.6f));// blueish
        for (Cell s : level.getSpawners()) {
            shapes.rect(s.x(), s.y(), 1, 1);
        }

        // draw doors and switches
        doorSystem.render(shapes);
        switchSystem.render(shapes);

        // draw start and exit
        shapes.setColor(new Color(0.1f, 0.9f, 0.2f, 0.35f)); // start green
        shapes.rect(level.getStart().x(), level.getStart().y(), 1, 1);
        shapes.setColor(new Color(0.1f, 1f, 0.3f, 0.85f)); // exit red
        shapes.rect(exitCell.x(), exitCell.y(), 1, 1);
        shapes.end();

        // draw entities
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        tether.render(shapes);
        yarns.render(shapes);
        enemies.render(shapes);
        player.render(shapes);

        // if you win, draw overlay
        if (win) {
            shapes.setColor(new Color(0f, 0f, 0f, 0.6f));
            shapes.rect(0,0,grid.getWidth(), grid.getHeight());
            shapes.setColor(new Color(1f,0.95f,0.2f,1f));
            shapes.circle(exitCell.x()+0.5f, exitCell.y()+0.5f, 0.5f, 24); // sparkles
        }
        shapes.end();

        // draw hud
        hud.setCheeseHp(tether.getCurrentHp(), tether.getMaxHp());
        hud.setLevelNumber(levelIndex);
        hud.render();
    }

    @Override
    public void resize (int width, int height) {
        viewport.update(width, height, true);
        if(hud != null){
            hud.resize(width, height);
        }
    }

    @Override
    public void dispose () {
        shapes.dispose();
        if (hud != null){
            hud.dispose();
        }
    }
}
