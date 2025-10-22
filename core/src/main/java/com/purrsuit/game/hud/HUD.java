package com.purrsuit.game.hud;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class HUD {

    private OrthographicCamera uiCam = new OrthographicCamera();
    private ScreenViewport uiViewport = new ScreenViewport(uiCam);
    private ShapeRenderer shapes = new ShapeRenderer();
    private BitmapFont font = new BitmapFont();
    private GlyphLayout layout = new GlyphLayout();
    private SpriteBatch batch = new SpriteBatch();

    // state variables
    private int currentHp = 3;
    private int maxHp = 3;
    private int levelNumber = 1;
    private boolean catnipActive = false;
    private float catnipRemaining = 0f;
    private float catnipMax = 0f;

    // config in pixels
    private static final float PADDING = 12f;
    private static final float DOT_RADIUS = 10f;
    private static final float DOT_SPACING = 26f; // center to center

    public HUD(){
        font.getData().setScale(1.6f);
    }

    // setters
    public void setCatnipTimer(boolean active, float remaining, float max){
        this.catnipActive = active;
        this.catnipRemaining = Math.max(0f, remaining);
        this.catnipMax = Math.max(0.1f, max);
    }

    public void setCheeseHp(int current, int max){
        this.maxHp = Math.max(1, max);
        this.currentHp = Math.max(0, Math.min(current, this.maxHp)); // clamp to [0, maxHp]
    }
    public void setLevelNumber(int levelNumber){
        this.levelNumber = Math.max(1, levelNumber);
    }

    // call on window resize
    public void resize(int width, int height){
        uiViewport.update(width, height, true);
    }

    public void render() {
        shapes.setProjectionMatrix(uiCam.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);

        float startX = PADDING + DOT_RADIUS;
        float y = uiViewport.getWorldHeight() - PADDING - DOT_RADIUS;

        for (int i = 0; i < maxHp; i++) {
            boolean filled = i < currentHp;
            if (filled) {
                shapes.setColor(Color.PINK);
                shapes.circle(startX + i * DOT_SPACING, y, DOT_RADIUS, 20);
            } else {
                // outline circle for lost hp
                shapes.setColor(new Color(1f, 1f, 0.6f, 0.7f)); // light yellow
                shapes.circle(startX + i * DOT_SPACING, y, DOT_RADIUS, 20); // outer circle
                shapes.setColor(new Color(0f, 0f, 0f, 0.8f)); // dark inner
                shapes.circle(startX + i * DOT_SPACING, y, DOT_RADIUS * 0.65f, 20); // smaller inner circle
            }
        }
        shapes.end();

        batch.setProjectionMatrix(uiCam.combined);

        // draw catnip timer
        if (catnipActive){
            String text = String.format("Catnip: %.1f s", catnipRemaining);
            layout.setText(font, text);

            float marginTop = 8f;
            float x = (uiViewport.getScreenWidth() - layout.width) / 2f;
            float yTxt = uiViewport.getScreenHeight() - marginTop;

            batch.begin();
            font.draw(batch, text, x, yTxt);
            batch.end();
        }

        // draw level number on top-right
        String txt = "Level " + levelNumber;
        layout.setText(font, txt);
        float tx = uiViewport.getWorldWidth() - PADDING - layout.width;
        float ty = uiViewport.getWorldHeight() - PADDING;

        batch.setProjectionMatrix(uiCam.combined);
        batch.begin();
        font.setColor(Color.WHITE);
        font.draw(batch, txt, tx, ty);
        batch.end();
    }

    public void dispose(){
        shapes.dispose();
        batch.dispose();
        font.dispose();
    }
}
