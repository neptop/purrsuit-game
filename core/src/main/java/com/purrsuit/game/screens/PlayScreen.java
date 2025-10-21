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
import com.purrsuit.game.io.LevelIO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.Texture;
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
    private AssetManager assets;
    private TextureAtlas atlas;
    private SpriteBatch batch;

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

        batch = new SpriteBatch();
        assets = new AssetManager();
        assets.load("atlas/sprites.atlas", TextureAtlas.class);
        assets.finishLoading();

        atlas = assets.get("atlas/sprites.atlas", TextureAtlas.class);
        for (Texture t : atlas.getTextures()) {
            t.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        }

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
        shapes.setProjectionMatrix(cam.combined); // remove later

        batch.setProjectionMatrix(cam.combined);
        batch.begin();

        // draw floor
        TextureAtlas.AtlasRegion floor = atlas.findRegion("Floor");
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                batch.draw(floor, x, y, 1f, 1f);
            }
        }

        // draw walls
        TextureAtlas.AtlasRegion wall = atlas.findRegion("Wall");
        for (int x = 0; x < grid.getWidth(); x++) {
            for (int y = 0; y < grid.getHeight(); y++) {
                if (grid.isWall(new Cell(x,y))) {
                    batch.draw(wall, x, y, 1f, 1f);
                }
            }
        }

        // draw spawners
        TextureAtlas.AtlasRegion spawnerTex = atlas.findRegion("Spawner");
        for (Cell s : level.getSpawners()) {
            batch.draw(spawnerTex, s.x(), s.y(), 1f, 1f);
        }

        // draw start and exit
        TextureAtlas.AtlasRegion startTex = atlas.findRegion("Start");
        batch.draw(startTex, level.getStart().x(), level.getStart().y(), 1f, 1f);
        TextureAtlas.AtlasRegion exitTex = atlas.findRegion("Exit");
        batch.draw(exitTex, level.getExit().x(), level.getExit().y(), 1f, 1f);

        // draw doors
        TextureAtlas.AtlasRegion doorTex = atlas.findRegion("DoorUpDown");
        for (Door d : doorSystem.allDoors()) {
            if (!d.isOpen()) {
                Cell dc = d.getCell();
                batch.draw(doorTex, dc.x(), dc.y(), 1f, 1f);
            }
        }

        // draw switches
        TextureAtlas.AtlasRegion switchTex = atlas.findRegion("Switch");
        for (Switch s : switchSystem.allSwitches()) {
            Cell sc = s.getCell();
            batch.draw(switchTex, sc.x(), sc.y(), 1f, 1f);
        }

        // regions for entities
        TextureAtlas.AtlasRegion catTex = atlas.findRegion("Cat_idle");
        TextureAtlas.AtlasRegion cheeseTex = atlas.findRegion("Cheese");
        TextureAtlas.AtlasRegion mouseTex = atlas.findRegion("Mouse");
        TextureAtlas.AtlasRegion yarnTex = atlas.findRegion("Yarn");

        // player
        {
            float px = player.getRenderX() - 0.5f;
            float py = player.getRenderY() - 0.5f;
            batch.draw(catTex, px, py, 1f, 1f);
        }

        // enemies
        for (EnemyChaserMice e : enemies.all()) {
            float ex = e.getRenderX() - 0.5f;
            float ey = e.getRenderY() - 0.5f;
            batch.draw(mouseTex, ex, ey, 1f, 1f);
        }

        // yarn projectiles
        for (YarnProjectile y : yarns.getProjectiles()) {
            float tipX = y.getRenderX() + 0.35f * y.getDirection().dx;
            float tipY = y.getRenderY() + 0.35f * y.getDirection().dy;
            batch.draw(yarnTex, tipX - 0.25f, tipY - 0.25f, 0.5f, 0.5f);
        }

        // tethered cheese
        Cell cheeseCell = tether.getCheeseCell();
        if (cheeseCell != null) {
            batch.draw(cheeseTex, cheeseCell.x(), cheeseCell.y(), 1f, 1f);
        }

        batch.end();

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        // draw tether
        tether.render(shapes);

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
        if (assets != null){
            assets.dispose();
        }
        if (batch != null) {
            batch.dispose();
        }
    }
}
