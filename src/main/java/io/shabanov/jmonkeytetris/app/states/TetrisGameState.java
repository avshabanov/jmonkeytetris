package io.shabanov.jmonkeytetris.app.states;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import io.shabanov.jmonkeytetris.controller.TetrisGameController;
import io.shabanov.jmonkeytetris.model.Tetrades;
import io.shabanov.jmonkeytetris.model.TetrisModel;
import io.shabanov.jmonkeytetris.util.PubSub;
import io.shabanov.jmonkeytetris.view.TetrisView;
import io.shabanov.jmonkeytetris.audio.TetrisSoundEffects;
import io.shabanov.jmonkeytetris.util.KeyBindings;
import io.shabanov.jmonkeytetris.util.audio.SoundEffectsStore;
import io.shabanov.jmonkeytetris.view.HudView;
import lombok.Getter;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Main game with falling blocks.
 */
public final class TetrisGameState extends BaseAppState {
    private SimpleApplication app;
    private final Random random = ThreadLocalRandom.current();
    private final PubSub.Manager pubSubManager = PubSub.defaultManager();
    private final TetrisModel model = new TetrisModel(pubSubManager, Tetrades.tetradeProviderFromRandom(random));
    private TetrisView tetrisView;
    private HudView hudView;
    private TetrisGameController controller;
    private DirectionalLight sun;
    private SoundEffectsStore soundEffectsStore;
    private final ActionListener appActionListener = (name, isPressed, tpf) -> {
        if (InputAction.PAUSE_OR_RESTART.matches(name) && !isPressed) {
            if (!controller.togglePause()) {
                controller.restart();
            }
        } else if (InputAction.ROTATE.matches(name) && !isPressed) {
            controller.moveTetrade(0, 1);
        } else if (InputAction.LEFT.matches(name) && !isPressed) {
            controller.moveTetrade(-1, 0);
        } else if (InputAction.RIGHT.matches(name) && !isPressed) {
            controller.moveTetrade(1, 0);
        } else if (InputAction.QUICK_SAVE.matches(name) && !isPressed) {
            controller.saveState();
        } else if (InputAction.QUICK_LOAD.matches(name) && !isPressed) {
            controller.loadState();
        } else if (InputAction.QUIT.matches(name) && !isPressed) {
            app.getStateManager().detach(this);
            app.getStateManager().attach(new TitleScreenState());
        } else if (InputAction.DOWN.matches(name)) {
            controller.setDownSpeedUp(isPressed);
        }
    };

    @Getter
    private enum InputAction implements KeyBindings.ActionDetails {
        LEFT("L", KeyInput.KEY_LEFT),
        RIGHT("R", KeyInput.KEY_RIGHT),
        DOWN("D", KeyInput.KEY_DOWN),
        ROTATE("Rt", KeyInput.KEY_UP),
        QUICK_SAVE("QSave", KeyInput.KEY_Q),
        QUICK_LOAD("QLoad", KeyInput.KEY_L),
        QUIT("Quit", KeyInput.KEY_ESCAPE),
        PAUSE_OR_RESTART("Pause", KeyInput.KEY_SPACE);

        final String code;
        final int defaultKey;

        InputAction(String code, int defaultKey) {
            this.code = code;
            this.defaultKey = defaultKey;
        }
    }

    @Override
    protected void initialize(Application app) {
        this.app = (SimpleApplication) app;

        if (soundEffectsStore == null) {
            soundEffectsStore = SoundEffectsStore.from(this.app.getAssetManager(), TetrisSoundEffects.values());
        }

        model.clear();
        tetrisView = new TetrisView(random, this.app.getAssetManager(), model);
        hudView = new HudView(this.app.getAssetManager(), this.app.getContext().getSettings());
        controller = new TetrisGameController(model, tetrisView, hudView, soundEffectsStore);

        this.app.getRootNode().attachChild(tetrisView.getHolderNode());
        this.app.getGuiNode().attachChild(hudView.getHolderNode());

        tetrisView.getHolderNode().setLocalTranslation(-5f, 0, 0);

        // Must add a light to make the lit object visible
        sun = new DirectionalLight();
        sun.setDirection(new Vector3f(-1,-2,-10).normalizeLocal());
        sun.setColor(ColorRGBA.White);
        this.app.getRootNode().addLight(sun);

        setUpCamera();

        KeyBindings.bindActions(InputAction.values(), this.app.getInputManager(), appActionListener);

        soundEffectsStore.play(TetrisSoundEffects.START);
    }

    @Override
    protected void cleanup(Application app) {
        KeyBindings.unbindActions(InputAction.values(), this.app.getInputManager(), appActionListener);

        model.clear();
        this.app.getRootNode().detachChild(tetrisView.getHolderNode());
        this.app.getRootNode().removeLight(sun);
        this.app.getGuiNode().detachChild(hudView.getHolderNode());

        controller = null;
        tetrisView = null;
        hudView = null;

        // finally remove app
        this.app = null;
    }

    @Override
    protected void onEnable() {
    }

    @Override
    protected void onDisable() {
    }

    @Override
    public void update(float tpf) {
        controller.update(tpf);
    }

    private void setUpCamera() {
        // assuming that field width < height, adjust camera position according to field's height
        // TODO: better computation taking into an account desired screen padding
        final Camera cam = this.app.getCamera();
        final float adjustedHeight = 1.2f * TetrisModel.DEFAULT_TETRIS_FIELD_HEIGHT;
        cam.setLocation(new Vector3f(0, adjustedHeight, adjustedHeight));
        cam.lookAt(new Vector3f(0, TetrisModel.DEFAULT_TETRIS_FIELD_HEIGHT * .6f, 0), new Vector3f(0, 1, 0));
    }
}
