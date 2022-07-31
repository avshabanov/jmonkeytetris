package io.shabanov.jmonkeytetris.app.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.system.AppSettings;
import io.shabanov.jmonkeytetris.model.Tetrades;
import io.shabanov.jmonkeytetris.model.TetrisCoordinate;
import io.shabanov.jmonkeytetris.util.KeyBindings;
import io.shabanov.jmonkeytetris.view.presentation.TetradeColors;
import lombok.Getter;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Title screen.
 */
public final class TitleScreenState extends BaseAppState {
    private static final float TITLE_SCREEN_PAUSE_INIT = 2f;
    private static final int NUM_FALLING_FIGURES = 30;

    private SimpleApplication app;
    private final Node titleScreenUi = new Node();
    private final Node titleScreenField = new Node();
    private final Node fallingNodeHolder = new Node();
    private Spatial tetrisTitle;
    private Quaternion tetrisTitleOriginalRotation;
    private float titleScreenPause;
    private final Random random = ThreadLocalRandom.current();

    private static final class Figure {
        private final Node holderNode = new Node();
        private float speedMultiple;
        private float remainingDistance;

        public Figure(Random random, Spatial prototypeSpatial, Material prototypeMaterial) {
            for (int i = 0; i < Tetrades.ELEMENTS_COUNT; ++i) {
                final Spatial block = prototypeSpatial.clone();
                final Material material = prototypeMaterial.clone();
                final ColorRGBA color = TetradeColors.CELL_CUBE_COLORS[random.nextInt(TetradeColors.CELL_CUBE_COLORS.length)];
                material.setColor("Diffuse", new ColorRGBA(color.r, color.g, color.b, 0.09f));
                block.setMaterial(material);
                holderNode.attachChild(block);
            }
        }

        void reset(Random random) {
            speedMultiple = 2 * random.nextFloat() + 0.1f;
            remainingDistance = 25f;
            float randomAngle = FastMath.TWO_PI * random.nextFloat();
            float randomDistance = 2f + random.nextFloat() * 20f;

            holderNode.setLocalTranslation(randomDistance * FastMath.cos(randomAngle), 0, randomDistance * FastMath.sin(randomAngle));

            final List<TetrisCoordinate> tetradeCoordinates = Tetrades.ORIENTATIONS.get(random.nextInt(Tetrades.ORIENTATIONS.size()));
            for (int i = 0; i < Tetrades.ELEMENTS_COUNT; ++i) {
                final TetrisCoordinate c = tetradeCoordinates.get(i);
                holderNode.getChild(i).setLocalTranslation(c.x * 2, c.y * 2, 0);
            }
        }

        void update(Random random, float tpf) {
            float distanceIncrement = speedMultiple * tpf;
            remainingDistance -= distanceIncrement;
            if (remainingDistance <= 0) {
                reset(random);
                return;
            }

            holderNode.move(0, distanceIncrement, 0);
        }
    }

    private Figure[] figures;

    private final ActionListener appActionListener = (name, isPressed, tpf) -> {
        if (InputAction.START.matches(name) && !isPressed) {
            app.getStateManager().detach(this);
            app.getStateManager().attach(new TetrisGameState());
        } else if (InputAction.STOP.matches(name) && !isPressed) {
            app.stop();
        }
    };

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;
        final AssetManager assetManager = this.app.getAssetManager();
        final AppSettings appSettings = this.app.getContext().getSettings();

        this.app.getGuiNode().attachChild(titleScreenUi);
        this.app.getRootNode().attachChild(titleScreenField);

        initializeMenuLabels(assetManager, appSettings);
        initializeLogo(assetManager);
        initializeFallingFigures(assetManager);

        setUpCamera();
        setUpLight();

        KeyBindings.bindActions(InputAction.values(), this.app.getInputManager(), appActionListener);
    }

    @Override
    protected void cleanup(Application app) {
        KeyBindings.unbindActions(InputAction.values(), this.app.getInputManager(), appActionListener);

        titleScreenUi.detachAllChildren();
        titleScreenField.detachAllChildren();

        this.app.getGuiNode().detachChild(titleScreenUi);
        this.app.getRootNode().detachChild(titleScreenField);

        this.app = null;
    }

    @Override
    protected void onEnable() {}

    @Override
    protected void onDisable() {}

    @Override
    public void update(float tpf) {
        for (final Figure figure : figures) {
            figure.update(random, tpf);
        }

        if (titleScreenPause > 0f) {
            titleScreenPause -= tpf;
            return;
        }

        // rotate game logo
        tetrisTitle.rotate(tpf * 0.1f, 0, 0);
    }

    //
    // Private
    //

    @Getter
    private enum InputAction implements KeyBindings.ActionDetails {
        STOP("Quit", KeyInput.KEY_ESCAPE),
        START("Start", KeyInput.KEY_SPACE);

        final String code;
        final int defaultKey;

        InputAction(String code, int defaultKey) {
            this.code = code;
            this.defaultKey = defaultKey;
        }
    }

    private void initializeMenuLabels(AssetManager assetManager, AppSettings appSettings) {
        final BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        float menuOffsetY = appSettings.getHeight() / 2f;
        {
            menuOffsetY -= 22f;
            final BitmapText welcomeText = new BitmapText(guiFont, false, false);
            welcomeText.setSize(guiFont.getCharSet().getRenderedSize());
            welcomeText.setColor(ColorRGBA.Orange);
            welcomeText.setText("Press [SPACE] to Start");
            welcomeText.setLocalTranslation((appSettings.getWidth() - welcomeText.getLineWidth()) / 2f, menuOffsetY, 0);
            titleScreenUi.attachChild(welcomeText);
        }

        {
            menuOffsetY -= 22f;
            final BitmapText stopText = new BitmapText(guiFont, false, false);
            stopText.setSize(guiFont.getCharSet().getRenderedSize());
            stopText.setColor(ColorRGBA.Orange);
            stopText.setText("Press [Esc] to Quit");
            stopText.setLocalTranslation((appSettings.getWidth() - stopText.getLineWidth()) / 2f, menuOffsetY, 0);
            titleScreenUi.attachChild(stopText);
        }
    }

    private void initializeLogo(AssetManager assetManager) {
        if (tetrisTitle == null) {
            tetrisTitle = assetManager.loadModel("Models/tetris-label/tetris-label.j3o");
            tetrisTitle.setLocalScale(0.5f);
            tetrisTitle.getLocalRotation().clone();
            tetrisTitle.rotate(0, FastMath.HALF_PI, 0);
            tetrisTitle.move(0, 0, 5);
            tetrisTitleOriginalRotation = tetrisTitle.getLocalRotation().clone();
        } else {
            // restore rotation position
            tetrisTitle.setLocalRotation(tetrisTitleOriginalRotation);
        }
        titleScreenField.attachChild(tetrisTitle);

        titleScreenPause = TITLE_SCREEN_PAUSE_INIT;
    }

    private void initializeFallingFigures(AssetManager assetManager) {
        if (figures == null) {
            final Spatial prototypeSpatial = assetManager.loadModel("Models/pebble.j3o");
            final Material prototypeMaterial = assetManager.loadMaterial("Materials/pebble.j3m");
            prototypeMaterial.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);

            figures = new Figure[NUM_FALLING_FIGURES];
            for (int i = 0; i < NUM_FALLING_FIGURES; ++i) {
                final Figure figure = new Figure(random, prototypeSpatial, prototypeMaterial);
                figures[i] = figure;
                fallingNodeHolder.attachChild(figure.holderNode);
            }

            fallingNodeHolder.rotate(-FastMath.HALF_PI, 0, 0);
            fallingNodeHolder.move(0, -5, 10);
        }

        for (final Figure figure : figures) {
            figure.reset(random);
        }
        titleScreenField.attachChild(fallingNodeHolder);
    }

    private void setUpLight() {
        final PointLight pointLight = new PointLight();
        pointLight.setColor(ColorRGBA.Yellow);
        pointLight.setRadius(4f);
        pointLight.setPosition(new Vector3f( 0, 10, 0));
        titleScreenField.addLight(pointLight);

        final DirectionalLight directionalLight = new DirectionalLight();
        directionalLight.setColor(ColorRGBA.White);
        directionalLight.setDirection(new Vector3f(4f, -4f, -4f).normalizeLocal());
        titleScreenField.addLight(directionalLight);
    }

    private void setUpCamera() {
        final Camera cam = this.app.getCamera();
        cam.setLocation(new Vector3f(0, 20, 0));
        cam.lookAt(new Vector3f(0, 1, 0), new Vector3f(0, 1, 0));
    }
}
