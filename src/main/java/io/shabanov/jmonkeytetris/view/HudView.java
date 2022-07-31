package io.shabanov.jmonkeytetris.view;

import com.jme3.asset.AssetManager;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.system.AppSettings;
import io.shabanov.jmonkeytetris.model.TetrisGamePhase;
import io.shabanov.jmonkeytetris.util.fadeout.FadeoutTextBlock;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public final class HudView {
    private final Node holderNode = new Node();
    private final BitmapText phaseAnnounceText;
    private final BitmapText scoreAnnounceText;
    private final FadeoutTextBlock notifications;


    public HudView(AssetManager assetManager, AppSettings appSettings) {
        final BitmapFont guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");

        phaseAnnounceText = new BitmapText(guiFont, false, false);
        phaseAnnounceText.setSize(guiFont.getCharSet().getRenderedSize());
        phaseAnnounceText.setLocalTranslation(10, appSettings.getHeight() - phaseAnnounceText.getLineHeight(), 0);
        holderNode.attachChild(phaseAnnounceText);

        final BitmapText scoreLabelText = new BitmapText(guiFont, false, false);
        scoreLabelText.setSize(guiFont.getCharSet().getRenderedSize());
        int scoreAnnounceTextY = appSettings.getHeight() - 50;
        scoreLabelText.setLocalTranslation(10, scoreAnnounceTextY, 0);
        scoreLabelText.setColor(ColorRGBA.Gray);
        scoreLabelText.setText("Score:");
        holderNode.attachChild(scoreLabelText);

        scoreAnnounceText = new BitmapText(guiFont, false, false);
        scoreAnnounceText.setSize(guiFont.getCharSet().getRenderedSize());
        scoreAnnounceTextY -= scoreLabelText.getLineHeight();
        scoreAnnounceText.setLocalTranslation(10, scoreAnnounceTextY, 0);
        scoreAnnounceText.setColor(ColorRGBA.Green);
        scoreAnnounceText.scale(1.65f);
        holderNode.attachChild(scoreAnnounceText);

        notifications = new FadeoutTextBlock(5, () -> {
            final BitmapText text = new BitmapText(guiFont, false, false);
            text.setSize(guiFont.getCharSet().getRenderedSize());
            return text;
        });
        holderNode.attachChild(notifications.getHolderNode());
        notifications.getHolderNode().setLocalTranslation(10, 40, 0);
    }

    public Node getHolderNode() {
        return holderNode;
    }

    public void addNotification(String notification) {
        notifications.pushText(ColorRGBA.White, notification);
    }

    public void announcePhase(TetrisGamePhase phase) {
        switch (phase) {
            case PAUSED:
                phaseAnnounceText.setColor(ColorRGBA.White);
                phaseAnnounceText.setText("PAUSED");
                break;

            case GAME_OVER:
                phaseAnnounceText.setColor(ColorRGBA.Red);
                phaseAnnounceText.setText("GAME OVER");
                break;

            default:
                phaseAnnounceText.setColor(ColorRGBA.Black);
                phaseAnnounceText.setText("");
        }
    }

    public void announceScore(int currentScore) {
        scoreAnnounceText.setText(String.format("%08d", currentScore));
    }

    public void update(float tpf) {
        notifications.update(tpf);
    }
}
