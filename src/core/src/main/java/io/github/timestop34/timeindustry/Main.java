package io.github.timestop34.timeindustry;

import com.badlogic.ashley.core.Engine;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import io.github.timestop34.timeindustry.mod.ModManager;
import io.github.timestop34.timeindustry.network.NetworkSystem;
import io.github.timestop34.timeindustry.screens.ConnectionScreen;

import java.io.File;

public class Main extends Game {
    private SpriteBatch batch;
    private Engine engine;
    private NetworkSystem netSystem;
    private Skin uiSkin;

    public static final String RUSSIAN_CHARACTERS =
            "АБВГДЕЁЖЗИЙКЛМНОПРСТУФХЦЧШЩЪЫЬЭЮЯ" +
                    "абвгдеёжзийклмнопрстуфхцчшщъыьэюя" +
                    "1234567890.,:;_!¡¿?\"'+-*/()[]={}";

    @Override
    public void create() {
        ModManager.loadMods(new File("mods"));
        batch = new SpriteBatch();
        uiSkin = createSharedSkin();
        setScreen(new ConnectionScreen(this));
    }

    private Skin createSharedSkin() {
        Skin skin = new Skin();

        // ---------- Шрифты ----------
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/LiberationSans-Regular.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 16;
        parameter.genMipMaps = true;
        parameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
        parameter.magFilter = Texture.TextureFilter.Linear;
        parameter.characters = FreeTypeFontGenerator.DEFAULT_CHARS + RUSSIAN_CHARACTERS;
        BitmapFont defaultFont = generator.generateFont(parameter);
        generator.dispose();

        generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/LiberationSans-Bold.ttf"));
        parameter.size = 24;
        BitmapFont titleFont = generator.generateFont(parameter);
        generator.dispose();

        skin.add("default-font", defaultFont, BitmapFont.class);
        skin.add("title-font", titleFont, BitmapFont.class);

        // ---------- Цвета ----------
        Color white = new Color(1, 1, 1, 1);
        Color lightGray = new Color(0.7f, 0.7f, 0.7f, 1);
        Color darkGray = new Color(0.3f, 0.3f, 0.3f, 1);
        skin.add("white", white, Color.class);
        skin.add("lightGray", lightGray, Color.class);
        skin.add("darkGray", darkGray, Color.class);

        // ---------- Текстуры для фона кнопок ----------
        Pixmap whitePixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        whitePixmap.setColor(white);
        whitePixmap.fill();
        Texture whiteTexture = new Texture(whitePixmap);
        whitePixmap.dispose();

        Pixmap grayPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        grayPixmap.setColor(lightGray);
        grayPixmap.fill();
        Texture grayTexture = new Texture(grayPixmap);
        grayPixmap.dispose();

        Drawable whiteDrawable = new TextureRegionDrawable(whiteTexture);
        Drawable grayDrawable = new TextureRegionDrawable(grayTexture);

        // ---------- Стиль TextButton ----------
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.up = whiteDrawable;
        buttonStyle.down = grayDrawable;
        buttonStyle.font = defaultFont;
        buttonStyle.fontColor = lightGray;
        skin.add("default", buttonStyle);

        // ---------- Стиль TextField ----------
        TextField.TextFieldStyle textFieldStyle = new TextField.TextFieldStyle();
        textFieldStyle.font = defaultFont;
        textFieldStyle.fontColor = lightGray;
        textFieldStyle.background = whiteDrawable;
        textFieldStyle.cursor = whiteDrawable;
        skin.add("default", textFieldStyle);

        // ---------- Стиль Label ----------
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = titleFont;
        labelStyle.fontColor = lightGray;
        skin.add("default", labelStyle);

        // ---------- Стиль ImageButton ----------
        ImageButton.ImageButtonStyle imageButtonStyle = new ImageButton.ImageButtonStyle();
        imageButtonStyle.up = whiteDrawable; // фон кнопки, если не задана картинка
        imageButtonStyle.down = grayDrawable;
        skin.add("default", imageButtonStyle);

        // ---------- Стиль ScrollPane ----------
        ScrollPane.ScrollPaneStyle scrollStyle = new ScrollPane.ScrollPaneStyle();
        // По умолчанию скроллбары отключены, но можно настроить
        skin.add("default", scrollStyle);

        // ---------- Стиль для кнопок паузы ----------
        Pixmap pauseBgPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pauseBgPixmap.setColor(0f, 0f, 0f, 0.68f); // black 68%
        pauseBgPixmap.fill();
        Texture pauseBgTexture = new Texture(pauseBgPixmap);
        pauseBgPixmap.dispose();

        Pixmap pauseBorderPixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pauseBorderPixmap.setColor(0f, 0f, 0f, 1f); // black
        pauseBorderPixmap.fill();
        Texture pauseBorderTexture = new Texture(pauseBorderPixmap);
        pauseBorderPixmap.dispose();

        Drawable pauseUp = new TextureRegionDrawable(new TextureRegion(pauseBgTexture));
        Drawable pauseDown = new TextureRegionDrawable(new TextureRegion(pauseBorderTexture)); // при нажатии можно затемнить

        TextButton.TextButtonStyle pauseStyle = new TextButton.TextButtonStyle();
        pauseStyle.up = pauseUp;
        pauseStyle.down = pauseDown;
        pauseStyle.font = defaultFont;
        pauseStyle.fontColor = Color.WHITE;
        pauseStyle.over = pauseUp; // можно сделать чуть светлее при желании
        skin.add("pause-button", pauseStyle);

        return skin;
    }

    public SpriteBatch getBatch() { return batch; }
    public void setEngine(Engine engine) { this.engine = engine; }
    public Engine getEngine() { return engine; }
    public void setNetworkSystem(NetworkSystem netSystem) { this.netSystem = netSystem; }
    public NetworkSystem getNetworkSystem() { return netSystem; }
    public Skin getSkin() { return uiSkin; }

    @Override
    public void dispose() {
        if (netSystem != null) {
            try {
                netSystem.dispose();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        batch.dispose();
        uiSkin.dispose();
        if (engine != null) engine.removeAllEntities();
    }
}