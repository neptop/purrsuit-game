package com.purrsuit.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.scenes.scene2d.ui.*;
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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.Input.Keys;
import com.purrsuit.game.debug.CheatConsole;
import com.purrsuit.game.hud.HUD;
import com.purrsuit.game.PurrsuitGame;

public class PlayScreen extends ScreenAdapter {

    // fields
    private PurrsuitGame game;
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
    private PowerUpSystem powerUps;
    private List<PowerUpPickup> pickups;
    private CoinSystem coins;
    private static final int REQUIRED_COINS = 3; // coins needed to exit level
    private enum PlayState{
        PLAYING,
        WON,
        GAMEOVER
    }
    private PlayState currentState = PlayState.PLAYING;
    private Stage overlayStage;
    private Skin overlaySkin;
    private Texture overlayWhite;
    private boolean overlayBuilt = false; // avoid rebuilding on every frame
    private boolean hasNextLevel = false;
    private CheatConsole console;

    public PlayScreen(PurrsuitGame game, int levelIndex) {
        this.game = game;
        this.levelIndex = levelIndex;
    }

    public void executeCheat(String raw) {
        String line = raw.trim();
        if (line.isEmpty()) return;

        String lower = line.toLowerCase();

        // help command
        if (lower.equals("help")) {
            console.print("Available commands:\n");
            console.print("  god               - cheese takes no damage\n");
            console.print("  level -<n>        - jump to n level (e.g. level -3\n");
            console.print("  clear             - Instantly win the level\n");
            console.print("  lose              - Instantly lose the level\n");
            console.print("  give catnip       - Grant catnip power-up 1 int front (if open)\n");
            console.print("  killall           - Remove all enemies\n");
            console.print("  help              - show this list\n");
            return;
        }

        // god mode
        if (lower.equals("god")) {
            tether.setInvulnerable(true);
            console.print("Godmode: ON\n");
            return;
        }

        // level -n
        if (lower.startsWith("level -")) {
            try {
                int n = Integer.parseInt(lower.substring("level -".length()).trim());
                if (n <= 0) throw new NumberFormatException();
                // check if level exists
                boolean exists = Gdx.files.internal("levels/Level" + n + ".txt").exists();
                if (!exists) {
                    console.print("Level " + n + " does not exist.\n");
                    return;
                }
                game.setScreen(new PlayScreen(game, n));
            } catch (NumberFormatException e) {
                console.print("Invalid level number.\n");
            }
            return;
        }

        // clear
        if (lower.equals("clear")) {
            hasNextLevel = Gdx.files.internal("levels/Level" + (levelIndex + 1) + ".txt").exists();
            showVictoryOverlay();
            console.print("Level Cleared!\n");
            return;
        }

        // lose
        if (lower.equals("lose")) {
            showGameOverOverlay();
            console.print("Game Over!\n");
            return;
        }

        // give catnip
        if (lower.equals("give catnip")) {
            Cell here = player.getCurrentCell();
            Direction dir = player.getDirection();
            if (dir == null) dir = Direction.UP;
            Cell ahead = here.next(dir);

            boolean open = grid.passable(ahead) && (doorSystem == null || !doorSystem.isBlocked(ahead))
                && !enemies.hasEnemyAt(ahead)
                && !tether.blocks(ahead);
            if (open) {
                pickups.add(new PowerUpPickup(PowerUpType.CATNIP, ahead, 20f));
                console.print("Catnip power-up granted in front of player.\n");
            } else {
                console.print("Cannot place catnip there. Space is blocked.\n");
            }
            return;
        }

        // killall
        if (lower.equals("killall")) {
            int removed = enemies.killAllEnemies(null);
            console.print("Removed " + removed + " enemies.\n");
            return;
        }

        // unknown command
        console.print("Unknown command, type 'help' for list of commands.\n");
    }

    private Texture makeWhiteTex() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Skin buildOverlaySkin(Texture white) {
        Skin s = new Skin();
        s.add("white", white, Texture.class);

        // label style
        BitmapFont f = new BitmapFont();
        f.getData().setScale(1.6f);
        s.add("titleFont", f);
        Label.LabelStyle ls = new Label.LabelStyle();
        ls.font = f;
        ls.fontColor = Color.WHITE;
        s.add("title", ls);

        // button style
        BitmapFont bf = new BitmapFont();
        s.add("font", bf);
        Drawable up = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.18f,0.18f,0.18f,1f));
        Drawable down = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.28f,0.28f,0.28f,1f));
        Drawable over = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.22f,0.22f,0.22f,1f));
        TextButton.TextButtonStyle tbs = new TextButton.TextButtonStyle(up, down, over, bf);
        tbs.fontColor = Color.WHITE;
        s.add("default", tbs);
        return s;
    }

    private void showGameOverOverlay(){
        if (overlayBuilt) return;
        overlayBuilt = true;
        currentState = PlayState.GAMEOVER;

        Gdx.input.setInputProcessor(overlayStage);

        // transparent background
        Image veil = new Image(new TextureRegionDrawable(new TextureRegion(overlayWhite)));
        veil.setColor(new Color(0f,0f,0f,0.6f));
        veil.setFillParent(true);
        overlayStage.addActor(veil);

        // ui table
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        overlayStage.addActor(root);

        Label title = new Label("Game Over", overlaySkin, "title");
        TextButton back = new TextButton("Return to Main Menu", overlaySkin);
        back.addListener(e -> {
            if (!back.isPressed()) return false;
            game.setScreen(new MainMenuScreen(game));
            return true;
        });

        root.defaults().pad(12f);
        root.add(title).padTop(18f).row();
        root.add(back).width(240f).height(56f);
    }

    private void showVictoryOverlay(){
        if (overlayBuilt) return;
        overlayBuilt = true;
        currentState = PlayState.WON;

        Gdx.input.setInputProcessor(overlayStage);

        // transparent background
        Image veil = new Image(new TextureRegionDrawable(new TextureRegion(overlayWhite)));
        veil.setColor(new Color(0f,0f,0f,0.6f));
        veil.setFillParent(true);
        overlayStage.addActor(veil);

        // ui table
        Table root = new Table();
        root.setFillParent(true);
        root.center();
        overlayStage.addActor(root);

        Label title = new Label("Level Cleared", overlaySkin, "title");
        String buttonText = hasNextLevel ? "Next Level" : "Return to Main Menu";
        TextButton action = new TextButton(buttonText, overlaySkin);
        action.addListener(e -> {
            if (!action.isPressed()) return false;
            if (hasNextLevel) {
                game.setScreen(new PlayScreen(game, levelIndex + 1));
            } else {
                game.setScreen(new MainMenuScreen(game));
            }
            return true;
        });

        root.defaults().pad(12f);
        root.add(title).padTop(18f).row();
        root.add(action).width(240f).height(56f);
    }

    @Override
    public void show() {
        // load level from ASCII map
        level = LevelIO.load("levels/Level" + levelIndex + ".txt");
        grid = level.getGrid();
        exitCell = level.getExit();

        // init overlay
        overlayStage = new Stage(new ScreenViewport());
        overlayWhite = makeWhiteTex();
        overlaySkin = buildOverlaySkin(overlayWhite);

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

        // cheat console
        console = new CheatConsole();
        console.setExecutor(this::executeCheat);
        console.print("Cheat Console Initialized.\nType 'help' for commands.\n");


        // door and switch systems
        doorSystem = new DoorSystem();
        switchSystem = new SwitchSystem();

        // coins
        coins = new CoinSystem(level.getCoins());

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

        powerUps = new PowerUpSystem();
        pickups = new ArrayList<>();

        // spawn player pass in composite blocker
        player = new Player(grid, level.getStart(), Direction.RIGHT, compositeBlocker, new StepListener(){
            @Override
            public void onEnter(Cell cell){
                tether.onHeadMoved(cell);
            }
        }, powerUps
        );

        yarns = new YarnSystem(grid);
        yarns.setBlocker(compositeBlocker);

        enemies = new EnemySystem(grid, doorSystem, powerUps);
        spawners = new ArrayList<EnemySpawner>();
        for (Cell spawnerCell : level.getSpawners()) {
            spawners.add(new EnemySpawner(spawnerCell, 5f));
        }

        yarns.setCollisionProbe(new YarnSystem.CollisionProbe() {
            @Override
            public boolean isColliding(Cell cell) {
                return enemies.hasEnemyAt(cell) || switchSystem.getSwitchIdAt(cell) != null;
            }
        });

        yarns.setImpactHook((impactCell, dir) -> {
            enemies.killEnemiesAt(impactCell, catnipCell -> {
                if (enemies.rollCatnip()) {
                    pickups.add(new PowerUpPickup(PowerUpType.CATNIP, catnipCell, 15f));
                }
            });
            Character sid = switchSystem.getSwitchIdAt(impactCell);
            if (sid != null) {
                boolean opened = doorSystem.toggleDoors(sid.charValue());
            }
        });
    }

    @Override
    public void render (float delta) {
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.1f, 1f); // dark blueish
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT); // clear the screen

        if(Gdx.input.isKeyJustPressed(Keys.F1) || Gdx.input.isKeyJustPressed(Keys.GRAVE)) {
            if (console.isVisible()) {
                console.hide();
                if (currentState != PlayState.PLAYING && overlayStage != null) {
                    Gdx.input.setInputProcessor(overlayStage); // game input
                }
            } else {
                console.show();
            }
        }

        // update only if running
        if (currentState == PlayState.PLAYING) {

            powerUps.update(delta);
            // pickup lifecycle and collection
            for (int i = pickups.size() - 1; i >= 0; i--) {
                PowerUpPickup p = pickups.get(i);
                p.update(delta);
                if (!p.isAlive()) {
                    pickups.remove(i);
                    continue;
                }
                if (p.getCell().equals(player.getCurrentCell())) {
                    powerUps.grant(p.getType());
                    pickups.remove(i);
                }
            }


            player.update(delta);
            coins.collect(player.getCurrentCell());

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

            if (tether.isNotActive()) {
                showGameOverOverlay();
            }

            if (currentState == PlayState.PLAYING && now.equals(exitCell)) {
                int required = Math.min(REQUIRED_COINS, coins.getTotalCoins());
                if (coins.getCollectedCoins() >= required) {
                    hasNextLevel = Gdx.files.internal("levels/Level" + (levelIndex + 1) + ".txt").exists();
                    showVictoryOverlay();
                }
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
        // draw coins
        TextureAtlas.AtlasRegion coinTex = atlas.findRegion("CoinTile");
        coins.render(batch, coinTex);

        // regions for entities
        TextureAtlas.AtlasRegion catTex = atlas.findRegion("Cat_idle");
        TextureAtlas.AtlasRegion cheeseTex = atlas.findRegion("Cheese");
        TextureAtlas.AtlasRegion mouseTex = atlas.findRegion("Mouse");
        TextureAtlas.AtlasRegion yarnTex = atlas.findRegion("Yarn");
        TextureAtlas.AtlasRegion tetherTex = atlas.findRegion("Tether");
        TextureAtlas.AtlasRegion catnipTex = atlas.findRegion("Catnip");

        // draw tether trail
        tether.render(batch, tetherTex);

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

        // power-ups
        for (PowerUpPickup p : pickups) {
            if (p.getType() == PowerUpType.CATNIP) {
                p.render(batch, catnipTex);
            }
        }

        batch.end();

        // draw hud
        hud.setCheeseHp(tether.getCurrentHp(), tether.getMaxHp());
        hud.setLevelNumber(levelIndex);
        hud.setCatnipTimer(powerUps.isCatnipActive(), powerUps.getCatnipTimeRemaining(), PowerUpSystem.CATNIP_DURATION);
        hud.render();

        if (currentState != PlayState.PLAYING) {
            overlayStage.act(delta);
            overlayStage.draw();
        }

        // draw console
        if(console != null && console.isVisible()){
            console.render(delta);
        }
    }

    @Override
    public void resize (int width, int height) {
        viewport.update(width, height, true);
        if(hud != null){
            hud.resize(width, height);
        }
        if(overlayStage != null){
            overlayStage.getViewport().update(width, height, true);
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
        if (overlayStage != null){
            overlayStage.dispose();
        }
        if (overlayWhite != null) {
            overlayWhite.dispose();
        }
        if (overlaySkin != null){
            overlaySkin.dispose();
        }
    }
}
