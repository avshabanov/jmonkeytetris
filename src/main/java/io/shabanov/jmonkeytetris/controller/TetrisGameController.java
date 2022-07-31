package io.shabanov.jmonkeytetris.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.shabanov.jmonkeytetris.audio.TetrisSoundEffects;
import io.shabanov.jmonkeytetris.model.TetrisGamePhase;
import io.shabanov.jmonkeytetris.model.TetrisModel;
import io.shabanov.jmonkeytetris.model.TetrisScoring;
import io.shabanov.jmonkeytetris.model.save.Persistence;
import io.shabanov.jmonkeytetris.model.save.SaveState;
import io.shabanov.jmonkeytetris.util.audio.SoundEffectsStore;
import io.shabanov.jmonkeytetris.view.HudView;
import io.shabanov.jmonkeytetris.view.TetrisView;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;

@Slf4j
@ParametersAreNonnullByDefault
public final class TetrisGameController {
    private final ObjectMapper mapper = new ObjectMapper();
    private final TetrisModel model;
    private final TetrisView tetrisView;
    private final HudView hudView;
    private final SoundEffectsStore soundEffectsStore;

    // current phase of the playfield
    private TetrisGamePhase phase = TetrisGamePhase.RUNNING;

    // a desired move speed, smaller is faster, uses the same units as moveTick below
    private float moveSpeed;

    // game loop speed
    private float moveTick = 0.0f;

    // indicates whether falling tetrade speed needs to be increased (e.g. when player doesn't want to wait
    // for a tetrade to fall)
    private boolean downSpeedUp = false;

    private boolean spawnNewTetrade = true;
    private final TetrisScoring tetrisScoring = new TetrisScoring();
    private float smoothOffset = 0;

    public TetrisGameController(TetrisModel model, TetrisView tetrisView, HudView hudView, SoundEffectsStore soundEffectsStore) {
        this.model = model;
        this.tetrisView = tetrisView;
        this.hudView = hudView;
        this.soundEffectsStore = soundEffectsStore;
        this.moveSpeed = .5f;

        // update listeners
        this.model.updateFieldListeners();

        tetrisScoring.setDifficultyLevel(0);
        tetrisScoring.setCurrentScore(0);
        hudView.announceScore(tetrisScoring.getCurrentScore());
    }

    public void moveTetrade(int dx, int dOrientation) {
        if (phase != TetrisGamePhase.RUNNING) {
            return;
        }

        if (!model.tryMoveTo(dx, dOrientation)) {
            return;
        }

        tetrisView.updateFallingTetrade(smoothOffset);
    }

    public void setDownSpeedUp(boolean value) {
        downSpeedUp = value;
    }

    public void restart() {
        model.clear();
        tetrisView.clear();

        setPhase(TetrisGamePhase.RUNNING);
        tetrisScoring.setCurrentScore(0);
        hudView.announceScore(tetrisScoring.getCurrentScore());

        spawnNewTetrade = true;
    }

    public boolean togglePause() {
        switch (phase) {
            case PAUSED:
                setPhase(TetrisGamePhase.RUNNING);
                soundEffectsStore.play(TetrisSoundEffects.START);
                return true;

            case RUNNING:
                setPhase(TetrisGamePhase.PAUSED);
                return true;
        }
        return false;
    }

    public void update(float tpf) {
        hudView.update(tpf);

        if (phase != TetrisGamePhase.RUNNING) {
            return; // nothing to update!
        }

        // update non-blocking animation
        tetrisView.updateStaticAnimation(tpf);

        if (spawnNewTetrade) {
            moveTick = 0f;
            spawnNewTetrade = false;

            // if we're unable to place a newly spawned tetrade, it means game is over!
            if (!model.spawnNewTetrade()) {
                setPhase(TetrisGamePhase.GAME_OVER);
                return;
            }

            tetrisView.setupFallingTetrade();
            tetrisView.updateNextTetrade();
            return;
        }

        // at this point we're running and we should try to move falling tetrade down
        moveTick += tpf;
        if (moveTick >= moveSpeed || downSpeedUp) {
            final TetrisModel.MoveResult moveResult = model.moveDown();
            if (moveResult.isLastMove()) {
                // request new tetrade
                spawnNewTetrade = true;
                final int clearedLineCount = moveResult.getClearedLines().size();
                tetrisScoring.clearLines(clearedLineCount);
                tetrisView.removeLines(moveResult.getClearedLines());
                hudView.announceScore(tetrisScoring.getCurrentScore());

                sendLineClearNotifications(clearedLineCount);

                // also reset downSpeedUp otherwise next tetrade will fall too fast
                downSpeedUp = false;
            } else {
                tetrisView.updateFallingTetrade(0f);
            }

            moveTick = 0f;
            smoothOffset = 0f;
        } else if (model.canMoveDown()) {
            smoothOffset = -moveTick / moveSpeed;
            tetrisView.updateFallingTetrade(smoothOffset);
        }
    }

    private void sendLineClearNotifications(int clearedLineCount) {
        if (clearedLineCount == 0) {
            soundEffectsStore.play(TetrisSoundEffects.CLICK);
        } else if (clearedLineCount < 4) {
            soundEffectsStore.play(TetrisSoundEffects.ERASE);
        } else {
            soundEffectsStore.play(TetrisSoundEffects.SWOOSH);
        }

        if (clearedLineCount == 3) {
            hudView.addNotification("Not bad!");
        } else if (clearedLineCount > 3) {
            hudView.addNotification("Well done!");
        }
    }

    public void saveState() {
        if (phase == TetrisGamePhase.GAME_OVER) {
            return;
        }

        hudView.addNotification(trySaveState() ? "Quick save succeeded" : "Quick save failed, see logs");
    }

    public void loadState() {
        phase = TetrisGamePhase.RUNNING;
        hudView.addNotification(tryLoadState() ? "Quick load succeeded" : "Quick load failed, see logs");
    }

    //
    // Private
    //

    private void setPhase(TetrisGamePhase phase) {
        this.phase = phase;
        this.hudView.announcePhase(phase);
    }

    private boolean trySaveState() {
        final Path statePath = Persistence.QUICK_SAVE_PATH;
        final File parentDir = statePath.getParent().toFile();
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                log.warn("Unable to create directory structure to hold quick save file");
                return false;
            }
        }

        final SaveState state = new SaveState();
        model.save(state);
        state.setMoveTick(moveTick);
        state.setCurrentScore(tetrisScoring.getCurrentScore());
        state.setDifficultyLevel(tetrisScoring.getDifficultyLevel());
        state.setSpawnNewTetrade(spawnNewTetrade);
        state.setSmoothOffset(smoothOffset);

        final File saveFile = Persistence.QUICK_SAVE_PATH.toFile();
        try (final FileOutputStream outputStream = new FileOutputStream(saveFile)) {
            mapper.writeValue(outputStream, state);
        } catch (IOException e) {
            log.warn("Unable to persist save state to {}", saveFile.getAbsolutePath(), e);
            return false;
        }

        return true;
    }

    private boolean tryLoadState() {
        final File saveFile = Persistence.QUICK_SAVE_PATH.toFile();
        final SaveState state;
        try (final FileInputStream inputStream = new FileInputStream(saveFile)) {
            state = mapper.readValue(inputStream, SaveState.class);
        } catch (IOException e) {
            log.warn("Unable to load save state from {}", saveFile.getAbsolutePath(), e);
            return false;
        }

        // at this point we can mutate current state and hope everything will be all right
        model.clear();
        tetrisView.clear();

        model.load(state); //< this should also indirectly lead to updating current view
        moveTick = state.getMoveTick();
        tetrisScoring.setCurrentScore(state.getCurrentScore());
        tetrisScoring.setDifficultyLevel(state.getDifficultyLevel());
        spawnNewTetrade = state.isSpawnNewTetrade();
        smoothOffset = state.getSmoothOffset();

        // also recover from game over state
        if (phase == TetrisGamePhase.GAME_OVER) {
            phase = TetrisGamePhase.RUNNING;
        }

        // update HUD
        hudView.announceScore(tetrisScoring.getCurrentScore());
        hudView.announcePhase(phase);

        // play start sound
        soundEffectsStore.play(TetrisSoundEffects.START);

        return true;
    }
}
