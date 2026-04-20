package io.github.timestop34.timeindustry.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PauseOverlay {
    private static final Logger logger = LoggerFactory.getLogger(PauseOverlay.class);

    private final Stage stage;
    private final Table mainTable;
    private final Skin skin;
    private final Runnable onResumeCallback;
    private boolean visible = false;

    public PauseOverlay(Skin skin, Runnable onResumeCallback) {
        this.skin = skin;
        this.onResumeCallback = onResumeCallback;
        this.stage = new Stage(new ScreenViewport());

        // Полупрозрачный фон
        Pixmap bgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        bgPixmap.setColor(0.118f, 0.118f, 0.118f, 0.65f); // #1E1E1E с альфой 65%
        bgPixmap.fill();
        Texture bgTexture = new Texture(bgPixmap);
        bgPixmap.dispose();
        Drawable bgDrawable = new TextureRegionDrawable(new TextureRegion(bgTexture));

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.setBackground(bgDrawable);
        stage.addActor(mainTable);

        // Контейнер для кнопок (горизонтальный ряд)
        Table buttonsTable = new Table();
        mainTable.add(buttonsTable).expand().center();

        // Кнопка "Продолжить"
        TextButton continueBtn = new TextButton("Продолжить", skin, "pause-button");
        continueBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                hide();
                onResumeCallback.run();
            }
        });

        // Кнопка "Сохранить Карту"
        TextButton saveBtn = new TextButton("Сохранить\nКарту", skin, "pause-button");
        saveBtn.getLabel().setAlignment(Align.center);
        saveBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                logger.info("Save map requested (stub)");
                // TODO: реализовать запрос сохранения карты
            }
        });

        // Кнопка "Выход"
        TextButton exitBtn = new TextButton("Выход", skin, "pause-button");
        exitBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                logger.info("Exit requested (stub)");
                // TODO: вернуться в главное меню или выйти из игры
                Gdx.app.exit();
            }
        });

        // Установка размера кнопок (пропорционально экрану)
        float btnSize = Math.min(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()) * 0.15f;
        buttonsTable.add(continueBtn).size(btnSize, btnSize).pad(20);
        buttonsTable.add(saveBtn).size(btnSize, btnSize).pad(20);
        buttonsTable.add(exitBtn).size(btnSize, btnSize).pad(20);
    }

    public void show() {
        visible = true;
        Gdx.input.setInputProcessor(stage);
        // Приостановка игрового цикла (если нужно)
        // Gdx.graphics.setContinuousRendering(false); // опционально
    }

    public void hide() {
        visible = false;
        Gdx.input.setInputProcessor(null); // вернём управление игре
        // Gdx.graphics.setContinuousRendering(true);
    }

    public boolean isVisible() {
        return visible;
    }

    public void render() {
        if (!visible) return;
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    public void dispose() {
        stage.dispose();
    }

    public Stage getStage() {
        return stage;
    }
}