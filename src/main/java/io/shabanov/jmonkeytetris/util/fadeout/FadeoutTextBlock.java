package io.shabanov.jmonkeytetris.util.fadeout;

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.function.Supplier;

/**
 * Represents a few lines of fading text intended for notification messages.
 */
@ParametersAreNonnullByDefault
public final class FadeoutTextBlock {
    private final Node holder = new Node();
    private final Deque<FadeoutText> visibleLines = new ArrayDeque<>();
    private final Deque<FadeoutText> fadedLines = new ArrayDeque<>();

    public FadeoutTextBlock(int numberOfLines, Supplier<BitmapText> bitmapTextSupplier) {
        for (int i = 0; i < numberOfLines; ++i) {
            fadedLines.push(new FadeoutText(bitmapTextSupplier.get()));
        }
    }

    public Node getHolderNode() {
        return holder;
    }

    public void pushText(ColorRGBA color, String text) {
        final FadeoutText destText;
        if (fadedLines.isEmpty()) {
            destText = visibleLines.removeLast();
        } else {
            destText = fadedLines.pop();
        }
        int i = 1;
        for (final FadeoutText line : visibleLines) {
            line.getBitmapText().setLocalTranslation(0, i * line.getBitmapText().getLineHeight(), 0);
            ++i;
        }

        destText.resetText(color, text);
        destText.getBitmapText().setLocalTranslation(0, 0, 0);
        visibleLines.addFirst(destText); // make text visible
        holder.attachChild(destText.getBitmapText());
    }

    public void update(float tpf) {
        for (final Iterator<FadeoutText> it = visibleLines.iterator(); it.hasNext();) {
            final FadeoutText text = it.next();
            if (!text.update(tpf)) {
                it.remove(); // remove from the current set of lines
                holder.detachChild(text.getBitmapText()); // make it invisible
                fadedLines.push(text);
            }
        }
    }
}
