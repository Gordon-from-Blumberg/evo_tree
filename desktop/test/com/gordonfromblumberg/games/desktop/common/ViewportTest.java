package com.gordonfromblumberg.games.desktop.common;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.*;

public class ViewportTest implements ApplicationListener {
    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static Skin SKIN;

    private static OrthographicCamera camera = new OrthographicCamera();

    private Viewport viewport;
    private Stage stage;

    public static void main(String... args) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        config.title = "Viewport test";
        new LwjglApplication(new ViewportTest(), config);
    }

    @Override
    public void create() {AssetManager assets = new AssetManager();
        assets.load("ui/uiskin.atlas", TextureAtlas.class);
        assets.load("ui/uiskin.json", Skin.class);
        assets.finishLoading();
        SKIN = assets.get("ui/uiskin.json", Skin.class);

        Gdx.graphics.setWindowedMode(WIDTH, HEIGHT);

        viewport = ViewportType.SCREEN.viewport;
        stage = new Stage(viewport);

        VerticalGroup group = new VerticalGroup();
        group.space(3);
        group.addActor(createButton("Screen viewport", ViewportType.SCREEN));
        group.addActor(createButton("Stretch viewport", ViewportType.STRETCH));
        group.addActor(createButton("Fill viewport", ViewportType.FILL));
        group.addActor(createButton("Fit viewport", ViewportType.FIT));

        Table table = new Table(SKIN);
        table.setFillParent(true);
        table.add(group).expand();
        stage.addActor(table);

        stage.setDebugAll(true);

        Gdx.input.setInputProcessor(stage);

        group.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                TextButton button = event.getTarget().firstAscendant(TextButton.class);
                Gdx.app.log("CLICK", "target = " + button);
                if (button == null)
                    return;

                viewport = ((ViewportType) button.getUserObject()).viewport;
                stage.setViewport(viewport);
                viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act();
        stage.draw();
    }

    enum ViewportType {
        SCREEN(new ScreenViewport(camera)),
        STRETCH(new StretchViewport(WIDTH, HEIGHT, camera)),
        FILL(new FillViewport(WIDTH, HEIGHT, camera)),
        FIT(new FitViewport(WIDTH, HEIGHT, camera)),
        ;

        private final Viewport viewport;

        ViewportType(Viewport viewport) {
            this.viewport = viewport;
        }
    }

    private TextButton createButton(String name, ViewportType type) {
        TextButton button = new TextButton(name, SKIN);
        button.setUserObject(type);
        return button;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
