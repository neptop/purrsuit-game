package com.purrsuit.game.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.purrsuit.game.PurrsuitGame;

public class HowToPlayScreen extends ScreenAdapter {
    private final PurrsuitGame game;
    private Stage stage;
    private ScreenViewport viewport;
    private Skin skin;
    private BitmapFont font;
    private BitmapFont titleFont;
    private Texture whiteTex;

    public HowToPlayScreen(PurrsuitGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewport = new ScreenViewport(new OrthographicCamera());
        stage = new Stage(viewport);
        font = new BitmapFont();
        titleFont = new BitmapFont();
        titleFont.getData().setScale(1.6f);
        whiteTex = makeWhiteTexture();
        skin = buildSkin(font, titleFont, whiteTex);

        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // title
        Label titleLabel = new Label("How to Play", skin, "title");
        titleLabel.setColor(Color.WHITE);

        // instructions
        String text =
            "•Movement: Use WASD to move. \n\n" +
            "• Yarn Attacks: Press SPACE to throw yarn. Hitting a mouse with yarn destroys it.\n\n" +
            "• Tether & Cheese: A cheese wedge is tethered to you a few tiles behind your path. " +
            "It counts as a solid object. Plan your route so you don't trap your cheese!\n\n" +
            "• Mice Target the Cheese: Mice try to reach your cheese. The cheese takes 3 hits and you lose.\n\n" +
            "• Bumping Mice: You can safely run into mice your cat is invulnerable. Colliding with a mouse removes it.\n\n" +
            "• Catnip Power-Up: Picking up catnip speeds you up and makes mice flee from the cheese for a short time.\n\n" +
            "• Switches & Doors: Shoot the small switch tiles to toggle their matching doors.\n\n" +
            "• Coins: Collect all 3 coins in a level before entering the Exit to finish.\n\n";

        Label body = new Label(text, skin);
        body.setWrap(true);
        body.setAlignment(Align.topLeft);

        ScrollPane scroll = new ScrollPane(body, skin);
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);

        // back button
        TextButton backButton = new TextButton("Back to Main Menu", skin);
        backButton.addListener(e -> {
           if (!backButton.isPressed()) return false;
           game.setScreen(new MainMenuScreen(game));
           return true;
        });

        // layout
        root.defaults().pad(12f);
        root.add(titleLabel).padTop(24f).row();
        root.add(scroll).grow().padLeft(24f).padRight(24f).row();
        root.add(backButton).width(200f).height(50f).padBottom(24f);
        // input
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (font != null) font.dispose();
        if (whiteTex != null) whiteTex.dispose();
        if (skin != null) skin.dispose();
        if (titleFont != null) titleFont.dispose();
    }

    // helpers
    private Texture makeWhiteTexture() {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return texture;
    }

    private Skin buildSkin(BitmapFont font, BitmapFont titleFont, Texture white) {
        Skin skin = new Skin();

        skin.add ("font", font, BitmapFont.class);
        skin.add("titleFont", titleFont, BitmapFont.class);
        skin.add("white", white, Texture.class);

        // base drawables
        Drawable panel = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0f,0f,0f,0f));
        Drawable btnUp = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.15f,0.15f,0.15f,1f));
        Drawable btnDown = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.25f,0.25f,0.25f,1f));
        Drawable btnOver = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.2f,0.2f,0.2f,1f));
        Drawable scrollBg = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.05f,0.05f,0.05f,1f));

        // Create a LabelStyle
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // Create a title LabelStyle
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        titleStyle.font = titleFont;
        titleStyle.fontColor = Color.WHITE;
        skin.add("title", titleStyle);

        // Create a TextButtonStyle
        TextButton.TextButtonStyle textButtonStyle = new TextButton.TextButtonStyle();
        textButtonStyle.up = btnUp;
        textButtonStyle.down = btnDown;
        textButtonStyle.over = btnOver;
        textButtonStyle.font = font;
        textButtonStyle.fontColor = Color.WHITE;
        skin.add("default", textButtonStyle);

        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        scrollStyle.background = scrollBg;
        skin.add("default", scrollStyle);

        // default background
        skin.add("default-background", panel);

        return skin;
    }
}
