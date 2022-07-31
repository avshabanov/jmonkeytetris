package io.shabanov.jmonkeytetris.util;

import com.jme3.input.InputManager;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import lombok.experimental.UtilityClass;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@UtilityClass public class KeyBindings {
    public interface ActionDetails {
        String getCode();
        int getDefaultKey();

        default boolean matches(String codeName) {
            return getCode().equals(codeName);
        }
    }

    public static <TAction extends ActionDetails> void bindActions(
            TAction[] actionValues,
            InputManager inputManager,
            ActionListener actionListener
    ) {
        final String[] mappingNames = new String[actionValues.length];
        for (int i = 0; i < actionValues.length; ++i) {
            final TAction action = actionValues[i];
            inputManager.addMapping(action.getCode(), new KeyTrigger(action.getDefaultKey()));
            mappingNames[i] = action.getCode();
        }
        inputManager.addListener(actionListener, mappingNames);
    }

    public static <TAction extends ActionDetails> void unbindActions(
            TAction[] actionValues,
            InputManager inputManager,
            ActionListener actionListener
    ) {
        for (final TAction action : actionValues) {
            inputManager.deleteMapping(action.getCode());
        }
        inputManager.removeListener(actionListener);
    }
}
