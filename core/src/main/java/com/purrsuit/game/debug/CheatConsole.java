package com.purrsuit.game.debug;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class CheatConsole {
    public interface Executor {
        void execute(String command);
    }

    private final Stage stage;
    private final Skin skin;
    private final TextField input;
    private final TextArea log;
    private final Table root;
    private boolean visible = false;
    private Executor executor;
    private ScrollPane scroll;

    public CheatConsole() {
        stage = new Stage(new ScreenViewport());
        skin = buildSkin();
        root = new Table();
        root.setFillParent(true);

        // semi transparent backdrop on the lower half
        Image backdrop = new Image(new TextureRegionDrawable(skin.get("white", TextureRegion.class)));
        backdrop.setColor(new Color(0f, 0f, 0f,0.65f));
        backdrop.setFillParent(true);
        stage.addActor(backdrop);

        // panel
        Table panel = new Table(skin);
        Drawable panelBg = new TextureRegionDrawable(skin.get("white", TextureRegion.class)).tint(new Color(0f,0f,0f,0.8f));
        panel.setBackground(panelBg);
        panel.pad(10f);

        log = new TextArea("", skin);
        log.setDisabled(true);
        log.setPrefRows(8);

        input = new TextField("", skin);
        input.setMessageText("Enter command...");
        input.setFocusTraversal(false);
        input.setTextFieldListener((textField, c) -> {
            if (c == '\n' || c == '\r') {
                String cmd = textField.getText().trim();
                if (!cmd.isEmpty() && executor != null) {
                    print("> " + cmd + "\n");
                    executor.execute(cmd);
                    textField.setText("");
                }
            }
        });

        ScrollPane scroll = new ScrollPane(log, skin);
        scroll.setFadeScrollBars(false);
        panel.add(scroll).width(800f).height(400f).row();
        panel.add(input).width(800f).height(32f).row();
        root.add(panel).expandX().fillX().bottom().pad(40f);
        stage.addActor(root);
    }

    private Skin buildSkin(){
        Skin s = new Skin();
        Pixmap pixmap = new Pixmap(1,1, Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture white = new Texture(pixmap);
        pixmap.dispose();
        s.add("whiteTex", white, Texture.class);
        s.add("white", new TextureRegion(white), TextureRegion.class);

        // panel background
        s.add("panel", new TextureRegion(white));

        BitmapFont font = new BitmapFont();
        s.add("default-font", font);

        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = font;
        textFieldStyle.fontColor = Color.WHITE;
        textFieldStyle.background = new TextureRegionDrawable(s.get("white", TextureRegion.class)).tint((Color.WHITE));
        textFieldStyle.background = new TextureRegionDrawable(s.get("white", TextureRegion.class)).tint(new Color(0f,0f,0f,0.6f));
        textFieldStyle.cursor = new TextureRegionDrawable(s.get("white", TextureRegion.class)).tint(Color.WHITE);
        textFieldStyle.cursor.setMinWidth(2f);

        s.add("default", textFieldStyle);
        TextArea.TextFieldStyle textAreaStyle = new TextField.TextFieldStyle(textFieldStyle);
        s.add("default", textAreaStyle, TextArea.TextFieldStyle.class);
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        s.add("default", scrollStyle);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        s.add("default", labelStyle);
        return s;
    }

    public void setExecutor(Executor ex){
        this.executor = ex;
    }

    public void print(String message){
        String text = log.getText();
        if(text == null || text.isEmpty()){
            log.setText(message);
        } else {
            log.setText(text + "\n" + message);
        }
        log.setCursorPosition(log.getText().length());
        if (scroll != null) {
            scroll.layout();
            scroll.setScrollPercentY(1f);
        }
        stage.act(0);
    }

    public void clear (){
        log.setText("");
    }

    public void show() {
        if (visible) return;
        visible = true;
        Gdx.input.setInputProcessor(stage);
        stage.setKeyboardFocus(input);
    }

    public void hide() {
        if (!visible) return;
        visible = false;
        Gdx.input.setInputProcessor(null);
    }

    public boolean isVisible(){
        return visible;
    }

    public Stage getStage(){
        return stage;
    }

    public void render(float delta) {
        stage.act(delta);
        stage.draw();
    }

    public void dispose(){
        stage.dispose();
    }
}
