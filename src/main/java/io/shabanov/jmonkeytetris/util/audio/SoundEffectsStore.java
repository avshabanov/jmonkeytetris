package io.shabanov.jmonkeytetris.util.audio;

import com.jme3.asset.AssetManager;
import com.jme3.audio.AudioData;
import com.jme3.audio.AudioNode;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@ParametersAreNonnullByDefault
public interface SoundEffectsStore {
    void play(SoundEffect effect);

    static <T extends SoundEffect> SoundEffectsStore from(AssetManager assetManager, T[] effects) {
        final Map<SoundEffect, AudioNode> nodes = new HashMap<>();
        for (final SoundEffect effect : effects) {
            final AudioNode audioData = new AudioNode(assetManager, effect.getResourcePath(),
                    effect.isBuffered() ? AudioData.DataType.Buffer : AudioData.DataType.Stream);
            nodes.put(effect, audioData);
        }
        return new DefaultSoundEffectsStore(nodes);
    }
}

@Slf4j
@ParametersAreNonnullByDefault
final class DefaultSoundEffectsStore implements SoundEffectsStore {
    final Map<SoundEffect, AudioNode> nodes;

    public DefaultSoundEffectsStore(Map<SoundEffect, AudioNode> nodes) {
        this.nodes = nodes;
    }

    @Override
    public void play(SoundEffect effect) {
        final AudioNode node = nodes.get(effect);
        if (node == null) {
            log.info("Unknown sound effect: {}", effect.getResourcePath());
            return;
        }
        node.play();
    }
}
