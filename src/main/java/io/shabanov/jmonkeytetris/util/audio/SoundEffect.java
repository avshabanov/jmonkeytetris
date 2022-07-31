package io.shabanov.jmonkeytetris.util.audio;

import javax.annotation.Nonnull;

public interface SoundEffect {
    default boolean isBuffered() {
        return true;
    }

    @Nonnull String getResourcePath();
}
