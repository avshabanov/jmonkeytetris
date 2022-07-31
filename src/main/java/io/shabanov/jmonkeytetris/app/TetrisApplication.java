package io.shabanov.jmonkeytetris.app;

import com.jme3.app.SimpleApplication;
import com.jme3.math.ColorRGBA;
import io.shabanov.jmonkeytetris.app.states.TetrisGameState;
import io.shabanov.jmonkeytetris.app.states.TitleScreenState;

public final class TetrisApplication extends SimpleApplication {

    @Override
    public void simpleInitApp() {
        setDisplayStatView(false);
        setDisplayFps(false);

        viewPort.setBackgroundColor(ColorRGBA.fromRGBA255(0x05, 0x02, 0x00, 0xff));

        setUpCamera();

        // Delete default Escape key binding
        if (inputManager.hasMapping(INPUT_MAPPING_EXIT)) {
            inputManager.deleteMapping(INPUT_MAPPING_EXIT);
        }

        if (Boolean.TRUE.toString().equals(System.getProperty("QUICK_GAME_START"))) {
            stateManager.attach(new TetrisGameState());
        } else {
            stateManager.attach(new TitleScreenState());
        }
    }

    //
    // Private
    //

    private void setUpCamera() {
        flyCam.setEnabled(false);
    }
}
