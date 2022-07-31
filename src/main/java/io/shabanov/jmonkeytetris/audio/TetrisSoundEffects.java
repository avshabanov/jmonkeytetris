package io.shabanov.jmonkeytetris.audio;

import io.shabanov.jmonkeytetris.util.audio.SoundEffect;

import javax.annotation.Nonnull;

public enum TetrisSoundEffects implements SoundEffect {
    CLICK("click"),
    ERASE("erase"),
    START("start"),
    SWOOSH("swoosh");

    final String resourcePath;

    TetrisSoundEffects(String resourceName) {
        this.resourcePath = String.format("Sound/%s.wav", resourceName);
    }

    @Nonnull @Override public String getResourcePath() { return resourcePath; }
}
