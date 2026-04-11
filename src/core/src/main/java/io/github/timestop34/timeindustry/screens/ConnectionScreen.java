package io.github.timestop34.timeindustry.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import io.github.timestop34.timeindustry.ClientNetworkManager;
import io.github.timestop34.timeindustry.Main;
import io.github.timestop34.timeindustry.network.NetworkSystem;

public class ConnectionScreen implements Screen {
    private final Main game;
    private Stage stage;
    private Skin skin;
    private Texture backgroundTexture;

    public ConnectionScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        skin = game.getSkin(); // берём готовый скин

        backgroundTexture = new Texture("background.png");

        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label titleLabel = new Label("TimeIndustry", skin);
        titleLabel.setFontScale(2f);
        table.add(titleLabel).padBottom(50).row();

        TextField ipField = new TextField("ws://localhost:8080", skin);
        table.add(ipField).width(300).padBottom(20).row();

        TextButton connectButton = new TextButton("Подключиться", skin);
        table.add(connectButton).width(200).padBottom(20).row();

        Label statusLabel = new Label("", skin);
        table.add(statusLabel).row();

        connectButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String ip = ipField.getText();
                statusLabel.setText("Подключаемся к " + ip + "...");
                connectToServer(ip, statusLabel);
            }
        });
    }

    private void connectToServer(String ip, Label statusLabel) {
        try {
            ClientNetworkManager clientNetManager = new ClientNetworkManager(ip);
            NetworkSystem netSystem = new NetworkSystem(clientNetManager, false);

            game.setEngine(new com.badlogic.ashley.core.Engine());
            netSystem.setEngine(game.getEngine());
            game.setNetworkSystem(netSystem);
            game.getEngine().addSystem(netSystem);
            statusLabel.setText("Подключено! Загрузка...");
            game.setScreen(new GameScreen(game));
        } catch (Exception e) {
            statusLabel.setText("Ошибка: " + e.getMessage());
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 0);
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
        stage.dispose();
        if (backgroundTexture != null) backgroundTexture.dispose();
        // skin не уничтожаем — он общий
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}