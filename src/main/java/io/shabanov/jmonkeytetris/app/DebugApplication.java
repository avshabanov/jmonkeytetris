package io.shabanov.jmonkeytetris.app;

import com.jme3.app.SimpleApplication;
import com.jme3.font.BitmapText;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.debug.Grid;
import com.jme3.util.TangentBinormalGenerator;
import io.shabanov.jmonkeytetris.model.Tetrades;
import io.shabanov.jmonkeytetris.model.TetrisModel;
import io.shabanov.jmonkeytetris.util.KeyBindings;
import io.shabanov.jmonkeytetris.util.PubSub;
import io.shabanov.jmonkeytetris.util.fadeout.FadeoutTextBlock;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * For prototyping purposes only.
 */
@Slf4j
public final class DebugApplication extends SimpleApplication {
    private BitmapText osdText;
    private FadeoutTextBlock fadeoutTextBlock;

    private final PubSub.Manager pubSubManager = PubSub.defaultManager();

    public void simpleInitApp() {
        viewPort.setBackgroundColor(ColorRGBA.fromRGBA255(0x11, 0x11, 0x11, 0xff));

        final TetrisModel model = new TetrisModel(pubSubManager, Tetrades.tetradeProviderFromRandom(new Random()));
        final float adjustedHeight = 1.2f * model.getFieldHeight();
        cam.setLocation(new Vector3f(0, adjustedHeight, adjustedHeight));
        cam.lookAt(new Vector3f(0, model.getFieldHeight() * .6f, 0), cam.getUp());
        flyCam.setMoveSpeed(20.0f);

        setUpGui();
        setupGrids(model);

        // Must add a light to make the lit object visible
        {
            sun = new DirectionalLight();
            sun.setDirection(new Vector3f(1,0,-2).normalizeLocal());
            sun.setColor(ColorRGBA.White);
            rootNode.addLight(sun);
        }

        KeyBindings.bindActions(InputAction.values(), inputManager, appActionListener);
    }

    @Getter private enum InputAction implements KeyBindings.ActionDetails {
        SELECT_NEXT("Next", KeyInput.KEY_N),
        UP("Up", KeyInput.KEY_I),
        LEFT("Left", KeyInput.KEY_J),
        RIGHT("Right", KeyInput.KEY_L),
        DOWN("Down", KeyInput.KEY_K);

        private final String code;
        private final int defaultKey;

        InputAction(String code, int defaultKey) {
            this.code = code;
            this.defaultKey = defaultKey;
        }
    }

    private final Node gridHolder = new Node();

    private final ActionListener appActionListener = (name, isPressed, tpf) -> {
        if (InputAction.SELECT_NEXT.matches(name) && !isPressed) {
            fadeoutTextBlock.pushText(ColorRGBA.Green, "Hola! Current time=" + System.currentTimeMillis());
        } else if (InputAction.UP.matches(name) && !isPressed) {
            gridHolder.move(0, 1, 0);
        } else if (InputAction.DOWN.matches(name) && !isPressed) {
            gridHolder.move(0, -1, 0);
        } else if (InputAction.LEFT.matches(name) && !isPressed) {
            gridHolder.move(-1, 0, 0);
        } else if (InputAction.RIGHT.matches(name) && !isPressed) {
            gridHolder.move(1, 0, 0);
        }
    };

    private void setupGrids(TetrisModel model) {
        rootNode.attachChild(gridHolder);

        {
            {
                final int fieldHorizontalDimension = model.getFieldWidth() + 1;
                final Geometry wireGrid = new Geometry("wireframe-grid-bottom", new Grid(4, fieldHorizontalDimension, 1f));
                final Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.getAdditionalRenderState().setWireframe(true);
                mat.setColor("Color", ColorRGBA.Yellow);
                wireGrid.setMaterial(mat);
                wireGrid.center().move(new Vector3f(-fieldHorizontalDimension / 2f, 0, 0));
                gridHolder.attachChild(wireGrid);
            }
            { // add grid left
                final Geometry wireGrid = new Geometry("wireframe-grid-left", new Grid(4, model.getFieldHeight(), 1f));
                final Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.getAdditionalRenderState().setWireframe(true);
                mat.setColor("Color", ColorRGBA.Red);
                wireGrid.setMaterial(mat);
                wireGrid.center().move(new Vector3f(0, 0, 0));
                wireGrid.rotate(0, 0,  FastMath.HALF_PI);
                gridHolder.attachChild(wireGrid);
            }
            { // add grid right
                final Geometry wireGrid = new Geometry("wireframe-grid-left", new Grid(4, model.getFieldHeight(), 1f));
                final Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                mat.getAdditionalRenderState().setWireframe(true);
                mat.setColor("Color", ColorRGBA.Green);
                wireGrid.setMaterial(mat);
                wireGrid.center().move(new Vector3f(model.getFieldWidth(), 0, 0));
                wireGrid.rotate(0, 0, FastMath.HALF_PI);
                gridHolder.attachChild(wireGrid);
            }

            gridHolder.setLocalTranslation(model.getFieldWidth(), -.5f, -1);
        }

        for (int i = 0; i < model.getFieldWidth(); ++i) {
            final Spatial spatial = newPebble(i, i);
            rootNode.attachChild(spatial); // put this node in the scene
        }
    }

    private void setUpGui() {
        setDisplayStatView(false);
        setDisplayFps(false);

        guiNode.detachAllChildren();

        guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        osdText = new BitmapText(guiFont, false, false);
        osdText.setSize(guiFont.getCharSet().getRenderedSize());
        //osdText.setText("");
        osdText.setColor(ColorRGBA.Orange);
        osdText.setLocalTranslation(200, osdText.getLineHeight(), 0);
        guiNode.attachChild(osdText);

        fadeoutTextBlock = new FadeoutTextBlock(5, () -> {
            final BitmapText text = new BitmapText(guiFont, false, false);
            text.setSize(guiFont.getCharSet().getRenderedSize());
            return text;
        });
        fadeoutTextBlock.getHolderNode().setLocalTranslation(50, 50, 0);
        fadeoutTextBlock.pushText(ColorRGBA.Green, "This is a test!");
        guiNode.attachChild(fadeoutTextBlock.getHolderNode());

        final Node testHolder = new Node();
        final BitmapText testText = new BitmapText(guiFont, false, false);
        testText.setSize(guiFont.getCharSet().getRenderedSize());
        testText.setText("COOL");
        testText.scale(4f);
        testText.setColor(ColorRGBA.Orange);
        testHolder.attachChild(testText);
        guiNode.attachChild(testHolder);
        testHolder.setLocalTranslation(250, 250, 0);
    }

    private Spatial newPebble(int x, int y) {
        final Spatial spatial = assetManager.loadModel("Models/pebble.j3o");
        TangentBinormalGenerator.generate(spatial);
        spatial.setLocalScale(0.5f);

        final Material mat = assetManager.loadMaterial("Materials/pebble.j3m");
        final int p = (100 + x * 10) % 255;
        mat.setColor("Diffuse", ColorRGBA.fromRGBA255(p, p, p, 0xff));//< optional
        spatial.setMaterial(mat);

        spatial.setLocalTranslation(x, y, -1);

        return spatial;
    }

    private DirectionalLight sun;
    private int x = 0;

    @Override
    public void simpleUpdate(float tpf) {
        x = (x + 1) % 100;
        if (sun != null) {
            sun.setDirection(new Vector3f(x / 100.0f, x / 100.0f, -2).normalizeLocal());
        }

        if (osdText != null && x == 1) {
            final Vector3f location = gridHolder.getLocalTranslation();
            osdText.setText(String.format("grid: pos=%s", location));
        }

        fadeoutTextBlock.update(tpf);
    }
}
