package io.github.timestop34.timeindustry.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import io.github.timestop34.timeindustry.Main;
import io.github.timestop34.timeindustry.network.NetworkSystem;
import io.github.timestop34.timeindustry.network.messages.StartBuildingCommand;
import io.github.timestop34.timeindustry.network.messages.StartBreakingCommand;
import io.github.timestop34.timeindustry.systems.RenderSystem;
import io.github.timestop34.timeindustry.ui.BlockSelectionUI;
import io.github.timestop34.timeindustry.ui.PauseOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GameScreen implements Screen, InputProcessor {
    private static final Logger logger = LoggerFactory.getLogger(GameScreen.class);

    private final Main game;
    private RenderSystem renderSystem;
    private NetworkSystem netSystem;
    private OrthographicCamera camera;
    private BlockSelectionUI blockUI;
    private InputMultiplexer inputMultiplexer;
    private PauseOverlay pauseOverlay;

    private boolean leftPressed, rightPressed, upPressed, downPressed;

    public GameScreen(Main game) {
        this.game = game;
    }

    @Override
    public void show() {
        renderSystem = new RenderSystem(game.getBatch());
        game.getEngine().addSystem(renderSystem);
        camera = renderSystem.camera;
        netSystem = game.getNetworkSystem();

        Skin skin = game.getSkin();
        try {
            blockUI = new BlockSelectionUI(skin);
        } catch (Exception e) {
            logger.error("Failed to create BlockSelectionUI", e);
            blockUI = null;
        }

        inputMultiplexer = new InputMultiplexer();
        if (blockUI != null) {
            inputMultiplexer.addProcessor(blockUI.getStage());
        }
        inputMultiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(inputMultiplexer);

        pauseOverlay = new PauseOverlay(skin, this::onPauseResume);

        logger.debug("GameScreen shown");
    }

    private void onPauseResume() {
        // Игра продолжается
        Gdx.input.setInputProcessor(inputMultiplexer);
    }

    @Override
    public void render(float delta) {

        pauseOverlay.render();

        renderSystem.setProcesses(netSystem.getLastProcesses());

        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.getEngine().update(delta);

        if (!pauseOverlay.isVisible()) {
            blockUI.render();
            // Движение камеры
            float moveX = 0, moveY = 0;
            if (leftPressed) moveX -= RenderSystem.CAMERA_SPEED * delta;
            if (rightPressed) moveX += RenderSystem.CAMERA_SPEED * delta;
            if (downPressed) moveY -= RenderSystem.CAMERA_SPEED * delta;
            if (upPressed) moveY += RenderSystem.CAMERA_SPEED * delta;
            renderSystem.camera.position.x += moveX;
            renderSystem.camera.position.y += moveY;
        }
    }

    @Override
    public void resize(int width, int height) {
        renderSystem.resize(width, height);
        blockUI.resize(width, height);
        pauseOverlay.resize(width, height);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Проверяем, попали ли в UI
        if (blockUI.getStage().hit(screenX, Gdx.graphics.getHeight() - screenY, true) != null) {
            return false;
        }

        Vector3 worldCoords = camera.unproject(new Vector3(screenX, screenY, 0));
        int tileX = (int)(worldCoords.x / RenderSystem.TILE_SIZE);
        int tileY = (int)(worldCoords.y / RenderSystem.TILE_SIZE);

        if (button == Input.Buttons.LEFT) {
            String blockId = blockUI.getSelectedBlockId();
            if (blockId == null) {
                logger.debug("No block selected");
                return false;
            }
            StartBuildingCommand cmd = new StartBuildingCommand();
            cmd.blockId = blockId;
            cmd.x = tileX;
            cmd.y = tileY;
            netSystem.sendCommand(cmd);
            logger.debug("Sent start_building {} at {},{}", blockId, tileX, tileY);
            return true;
        } else if (button == Input.Buttons.RIGHT) {
            StartBreakingCommand cmd = new StartBreakingCommand();
            cmd.x = tileX;
            cmd.y = tileY;
            netSystem.sendCommand(cmd);
            logger.debug("Sent start_breaking at {},{}", tileX, tileY);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.ESCAPE) {
            if (pauseOverlay.isVisible()) {
                pauseOverlay.hide();
                onPauseResume();
            } else {
                pauseOverlay.show();
                // Приостановка ввода игры
                Gdx.input.setInputProcessor(pauseOverlay.getStage());
            }
            return true;
        }

        return switch (keycode) {
            case Input.Keys.A -> {
                leftPressed = true;
                yield true;
            }
            case Input.Keys.D -> {
                rightPressed = true;
                yield true;
            }
            case Input.Keys.W -> {
                upPressed = true;
                yield true;
            }
            case Input.Keys.S -> {
                downPressed = true;
                yield true;
            }
            default -> false;
        };
    }

    @Override
    public boolean keyUp(int keycode) {
        switch (keycode) {
            case Input.Keys.A: leftPressed = false; return true;
            case Input.Keys.D: rightPressed = false; return true;
            case Input.Keys.W: upPressed = false; return true;
            case Input.Keys.S: downPressed = false; return true;
        }
        return false;
    }

    @Override public boolean keyTyped(char character) { return false; }
    @Override public boolean touchUp(int screenX, int screenY, int pointer, int button) { return false; }

    @Override public boolean touchCancelled(int screenX, int screenY, int pointer, int button) { return false; }
    @Override public boolean touchDragged(int screenX, int screenY, int pointer) { return false; }
    @Override public boolean mouseMoved(int screenX, int screenY) { return false; }
    @Override public boolean scrolled(float amountX, float amountY) { return false; }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        Gdx.input.setInputProcessor(null);
        if (renderSystem != null) renderSystem.dispose();
        if (blockUI != null) blockUI.dispose();
        if (pauseOverlay != null) pauseOverlay.dispose();
    }
}