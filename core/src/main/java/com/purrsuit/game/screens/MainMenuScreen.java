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
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.purrsuit.game.PurrsuitGame;

public class MainMenuScreen extends ScreenAdapter {
    private final PurrsuitGame game;
    private Stage stage;
    private ScreenViewport viewport;
    private Skin skin;
    private BitmapFont font;
    private Texture whiteTex;

    public MainMenuScreen(PurrsuitGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        viewport = new ScreenViewport(new OrthographicCamera());
        stage = new Stage(viewport);
        font = new BitmapFont();
        whiteTex = makeWhiteTexture();
        skin = buildSkin(font, whiteTex);

        // layout
        Table root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        // title
        Label titleLabel = new Label("Purrsuit", skin, "title");
        titleLabel.setColor(Color.WHITE);

        // buttons
        TextButton playButton = new TextButton("Play", skin);
        TextButton howToPlayButton = new TextButton("How to Play", skin);
        TextButton exitButton = new TextButton("Exit", skin);

        // add listeners
        playButton.addListener(e -> {
            if(!playButton.isPressed()) return false; // only trigger on press
            game.setScreen(new PlayScreen());
            return true;
        });

        howToPlayButton.addListener(e -> {
            if (!howToPlayButton.isPressed()) return false;
            game.setScreen(new HowToPlayScreen(game));
            return true; // do nothing for now
        });

        exitButton.addListener(e -> {
            if (!exitButton.isPressed()) return false;
            Gdx.app.exit();
            return true;
        });

        // assemble layout
        root.defaults().pad(10f);
        root.add(titleLabel).padTop(40f).row();
        root.add(playButton).width(240f).height(56f).row();
        root.add(howToPlayButton).width(240f).height(56f).row();
        root.add(exitButton).width(240f).height(56f).row();

        // input
        Gdx.input.setInputProcessor(stage);
    }

    @Override
    public void render(float delta) {
        // clear to black
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        if (stage != null) stage.dispose();
        if (font != null) font.dispose();
        if (whiteTex != null) whiteTex.dispose();
        if (skin != null) skin.dispose();
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

    private Skin buildSkin(BitmapFont font, Texture white) {
        Skin skin = new Skin();

        skin.add ("font", font, BitmapFont.class);
        skin.add("white", white, Texture.class);

        // base drawables
        Drawable bg = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0f,0f,0f,0f));
        Drawable btnUp = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.15f,0.15f,0.15f,1f));
        Drawable btnDown = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.25f,0.25f,0.25f,1f));
        Drawable btnOver = new TextureRegionDrawable(new TextureRegion(white)).tint(new Color(0.2f,0.2f,0.2f,1f));

        // Create a LabelStyle
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = font;
        labelStyle.fontColor = Color.WHITE;
        skin.add("default", labelStyle);

        // Create a title LabelStyle
        Label.LabelStyle titleStyle = new Label.LabelStyle();
        BitmapFont titleFont = new BitmapFont();
        titleFont.getData().setScale(2.2f);
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

        // default background
        skin.add("default-bg", bg);

        return skin;
    }
}
