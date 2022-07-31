package io.shabanov.jmonkeytetris.util.fadeout;

import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class FadeoutText {
    private static final float FADE_RATE = 0.5f;

    private final BitmapText bitmapText;
    private float fadeAlpha;
    private ColorRGBA textColor = ColorRGBA.White;

    public FadeoutText(BitmapText bitmapText) {
        this.bitmapText = bitmapText;
    }

    public BitmapText getBitmapText() {
        return bitmapText;
    }

    public void resetText(ColorRGBA textColor, String text) {
        fadeAlpha = 1.0f;
        this.textColor = textColor;
        bitmapText.setText(text);
        bitmapText.setColor(textColor);
    }

    public boolean update(float tpf) {
        fadeAlpha -= FADE_RATE * tpf;
        if (fadeAlpha <= 0f) {
            return false;
        }

        bitmapText.setColor(new ColorRGBA(textColor.r, textColor.g, textColor.b, fadeAlpha));
        return true;
    }
}
