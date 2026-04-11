package io.github.timestop34.timeindustry.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
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
import io.github.timestop34.timeindustry.world.block.Block;
import io.github.timestop34.timeindustry.world.registry.BlockRegistry;
import io.github.timestop34.timeindustry.world.block.Category;
import io.github.timestop34.timeindustry.world.registry.CategoryRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

public class BlockSelectionUI {
    private static final Logger logger = LoggerFactory.getLogger(BlockSelectionUI.class);

    private final Stage stage;
    private final Skin skin;
    private final Table mainTable;
    private final Table blocksTable;
    private final Label selectedBlockLabel;
    private final Image selectedBlockImage;
    private final Table categoryButtonsTable;
    private final Label categoryNameLabel;
    private String selectedBlockId;
    private String currentCategoryId;
    private float panelWidth, panelHeight;
    private int blockColumns = 6;

    public BlockSelectionUI(Skin skin) {
        this.skin = skin;
        this.stage = new Stage(new ScreenViewport());

        mainTable = new Table();
        mainTable.setFillParent(true);
        mainTable.bottom().right().pad(10);
        stage.addActor(mainTable);

        Table panel = new Table();
        panel.setBackground(createColorDrawable(0.1f, 0.1f, 0.1f, 0.85f));
        mainTable.add(panel).size(panelWidth, panelHeight).pad(10);

        // Верхняя строка
        Table topRow = new Table();
        panel.add(topRow).expandX().fillX().pad(5).row();

        selectedBlockImage = new Image();
        topRow.add(selectedBlockImage).size(32, 32).padRight(8);

        selectedBlockLabel = new Label("No block", skin, "default-font", Color.LIGHT_GRAY);
        topRow.add(selectedBlockLabel).expandX().align(Align.left);

        TextButton helpButton = new TextButton("?", skin);
        helpButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                logger.debug("Help clicked");
            }
        });
        topRow.add(helpButton).size(32, 32).padLeft(8);

        // Название категории
        categoryNameLabel = new Label("", skin, "default-font", Color.WHITE);
        panel.add(categoryNameLabel).expandX().fillX().pad(5).row();

        // Разделитель
        Image separator = new Image(createColorDrawable(0.5f, 0.5f, 0.5f, 1f));
        panel.add(separator).expandX().fillX().height(2).pad(2).row();

        // Основное содержимое
        Table contentRow = new Table();
        panel.add(contentRow).expand().fill().pad(5).row();

        blocksTable = new Table();
        ScrollPane scrollPane = new ScrollPane(blocksTable, skin);
        scrollPane.setFadeScrollBars(false);
        contentRow.add(scrollPane).expand().fill().padRight(5);

        categoryButtonsTable = new Table();
        contentRow.add(categoryButtonsTable).width(70).fillY();

        rebuildCategoryButtons();

        // Выбор первой категории
        Collection<Category> categories = CategoryRegistry.getAll().values();
        if (!categories.isEmpty()) {
            Category firstCat = categories.iterator().next();
            setCurrentCategory(firstCat.id());
        }

        updateLayout();
    }

    private void updateLayout() {
        float screenWidth = Gdx.graphics.getWidth();
        float screenHeight = Gdx.graphics.getHeight();

        panelWidth = Math.min(screenWidth * 0.4f, 700);
        panelHeight = Math.min(screenHeight * 0.5f, 500);

        if (panelWidth >= 600) blockColumns = 8;
        else if (panelWidth >= 500) blockColumns = 7;
        else blockColumns = 6;

        Cell<?> cell = mainTable.getCell(mainTable.getChildren().get(0));
        if (cell != null) {
            cell.size(panelWidth, panelHeight);
        }

        if (currentCategoryId != null) {
            setCurrentCategory(currentCategoryId);
        }
    }

    private Drawable createColorDrawable(float r, float g, float b, float a) {
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        pixmap.setColor(r, g, b, a);
        pixmap.fill();
        Texture texture = new Texture(pixmap);
        pixmap.dispose();
        return new TextureRegionDrawable(new TextureRegion(texture));
    }

    private Drawable loadDrawable(String path) {
        try {
            if (Gdx.files.internal(path).exists()) {
                Texture texture = new Texture(Gdx.files.internal(path));
                return new TextureRegionDrawable(new TextureRegion(texture));
            }
        } catch (Exception e) {
            logger.warn("Failed to load texture: {}", path);
        }
        return createColorDrawable(0.8f, 0.2f, 0.8f, 1f);
    }

    private void rebuildCategoryButtons() {
        categoryButtonsTable.clear();
        for (Category cat : CategoryRegistry.getAll().values()) {
            Drawable icon = loadDrawable(cat.iconPath());
            ImageButton button = new ImageButton(icon);
            button.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    setCurrentCategory(cat.id());
                }
            });
            categoryButtonsTable.add(button).size(48, 48).pad(2).row();
        }
    }

    private void setCurrentCategory(String catId) {
        currentCategoryId = catId;
        Category cat = CategoryRegistry.get(catId);
        if (cat == null) return;
        categoryNameLabel.setText(cat.name());

        blocksTable.clear();
        List<Block> blocks = cat.getBlocks();
        int i = 0;
        for (Block block : blocks) {
            ImageButton btn = createBlockButton(block);
            blocksTable.add(btn).size(42, 42).pad(2);
            if (++i % blockColumns == 0) {
                blocksTable.row();
            }
        }

        // Автовыбор первого блока в категории, если ничего не выбрано
        if (selectedBlockId == null && !blocks.isEmpty()) {
            selectBlock(blocks.get(0).getId());
        }
    }

    private ImageButton createBlockButton(Block block) {
        Drawable icon = loadDrawable(block.getTexturePath());
        ImageButton button = new ImageButton(icon);
        button.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                selectBlock(block.getId());
            }
        });
        return button;
    }

    public void selectBlock(String blockId) {
        this.selectedBlockId = blockId;
        Block block = BlockRegistry.get(blockId);
        if (block != null) {
            selectedBlockLabel.setText(block.getProperties().getName());
            selectedBlockImage.setDrawable(loadDrawable(block.getTexturePath()));
        }
    }

    public String getSelectedBlockId() {
        return selectedBlockId;
    }

    public void render() {
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        updateLayout();
    }

    public void dispose() {
        stage.dispose();
    }

    public Stage getStage() {
        return stage;
    }
}