package io.shabanov.jmonkeytetris.model.save;

import io.shabanov.jmonkeytetris.model.TetrisCoordinate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public final class SaveState {
    //
    // Model state
    //

    private long[][] cells;
    private TetrisCoordinate tetradeCenter;
    private int tetradeOffset;
    private int nextTetradeOffset;
    private int tetradeOrientation;

    //
    // Controller state
    //

    private float moveTick;
    private boolean spawnNewTetrade;
    private float smoothOffset;
    private int currentScore;
    private int difficultyLevel;
}
