package io.shabanov.jmonkeytetris.model;

import io.shabanov.jmonkeytetris.model.save.SaveState;
import io.shabanov.jmonkeytetris.util.PubSub;
import lombok.Value;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * A logical model of a tetris field along with the respective operations.
 * This is a central entity in the game that encodes its logical state.
 *
 * This determines common operations that could be performed by a game controller
 * upon a tetris field which is logically comprised of a current moving tetrade, tetrade projection,
 * the next moving tetrade and finally a set of playfield cells.
 *
 * The operations on a playfield include spawning a new tetrade, moving down a current tetrade, retrieving status of
 * the current playfield etc.
 */
public final class TetrisModel {

    public static final int DEFAULT_TETRIS_FIELD_WIDTH = 10;
    public static final int DEFAULT_TETRIS_FIELD_HEIGHT = 22;
    private static final TetrisCoordinate DEFAULT_SPAWNING_POINT = TetrisCoordinate.of(DEFAULT_TETRIS_FIELD_WIDTH / 2, DEFAULT_TETRIS_FIELD_HEIGHT - Tetrades.BOUNDING_BOX_RADIUS);

    private final Function<Integer, Integer> nextTetradeProvider;

    private long cellIDCounter = 1L;

    private final PubSub.Manager pubSubManager;

    @Value
    public static class FieldSetEvent implements PubSub.Event {
        List<TetrisCoordinate> coordinates;
    }

    public interface FieldChangeListener {
        void putCell(int x, int y);
    }

    public TetrisModel(PubSub.Manager pubSubManager, Function<Integer, Integer> nextTetradeProvider) {
        this.pubSubManager = pubSubManager;
        this.nextTetradeProvider = nextTetradeProvider;
    }

    public void clear() {
        for (int y = 0; y < getFieldHeight(); ++y) {
            for (int x = 0; x < getFieldWidth(); ++x) {
                cells[y][x] = 0;
            }
        }

        tetrade = null;
        nextTetrade = null;
    }

    public void putFieldAt(int x, int y) {
        if (x < 0 || x >= DEFAULT_TETRIS_FIELD_WIDTH) {
            throw new IllegalArgumentException("x");
        }
        if (y < 0 || y >= DEFAULT_TETRIS_FIELD_HEIGHT) {
            throw new IllegalArgumentException("y");
        }
        cells[y][x] = ++cellIDCounter;
    }

    //
    // Field State
    //

    public long getFieldAt(int x, int y) {
        if (x < 0 || x >= DEFAULT_TETRIS_FIELD_WIDTH) {
            throw new IllegalArgumentException("x");
        }
        if (y < 0 || y >= DEFAULT_TETRIS_FIELD_HEIGHT) {
            throw new IllegalArgumentException("y");
        }
        return cells[y][x];
    }

    public int getFieldWidth() {
        return DEFAULT_TETRIS_FIELD_WIDTH;
    }

    public int getFieldHeight() {
        return DEFAULT_TETRIS_FIELD_HEIGHT;
    }

    @Nullable
    public TetrisCoordinate getTetradeCenter() {
        return tetradeCenter;
    }

    public void addFieldChangeListener(FieldChangeListener listener) {
        fieldChangeListeners.add(listener);
    }

    public boolean spawnNewTetrade() {
        if (tetrade != null) {
            return true;
        }

        tetrade = generateNextTetrade();
        tetradeOrientation = 0;
        tetradeCenter = TetrisCoordinate.of(DEFAULT_SPAWNING_POINT);

        recomputeProjectedCoordinate();

        return canMoveTo(tetradeCenter.x, tetradeCenter.y, tetradeOrientation);
    }

    public void updateFieldListeners() {
        for (final FieldChangeListener listener : fieldChangeListeners) {
            for (int y = 0; y < getFieldHeight() - 2; ++y) {
                for (int x = 0; x < getFieldWidth(); ++x) {
                    if (cells[y][x] != 0) {
                        listener.putCell(x, y);
                    }
                }
            }
        }
    }

    public interface MoveResult {
        default boolean isLastMove() {
            return false;
        }
        default List<Integer> getClearedLines() {
            return List.of();
        }

        MoveResult CONTINUE_MOVING = new MoveResult() {};

        static MoveResult fromClearedLines(List<Integer> linesToClear) {
            return new MoveResult() {
                @Override public boolean isLastMove() {
                    return true;
                }

                @Override public List<Integer> getClearedLines() {
                    return linesToClear;
                }
            };
        }
    }

    public MoveResult moveDown() {
        if (tetrade == null) {
            throw new IllegalStateException("no active tetrade");
        }

        final TetrisCoordinate newCenter = TetrisCoordinate.of(tetradeCenter.x, tetradeCenter.y - 1);
        if (canMoveTo(newCenter.x, newCenter.y, tetradeOrientation)) {
            tetradeCenter = newCenter;
            recomputeProjectedCoordinate();
            return MoveResult.CONTINUE_MOVING;
        }

        // apply the tetrade
        int minY = tetradeCenter.y;
        int maxY = tetradeCenter.y;
        final List<TetrisCoordinate> offsets = cellOffsets(tetradeOrientation);
        for (final TetrisCoordinate offset : offsets) {
            final int xCell = tetradeCenter.x + offset.x;
            final int yCell = tetradeCenter.y + offset.y;
            maxY = Math.max(maxY, yCell);
            minY = Math.min(minY, yCell);
            putFieldAt(xCell, yCell);

            for (final FieldChangeListener listener : fieldChangeListeners) {
                listener.putCell(xCell, yCell);
            }
        }

        final List<Integer> linesToClear = checkLinesToClear(minY, maxY);
        clearLines(linesToClear);

        tetrade = null;
        return MoveResult.fromClearedLines(linesToClear);
    }

    public boolean canMoveDown() {
        return canMoveTo(tetradeCenter.x, tetradeCenter.y - 1, tetradeOrientation);
    }

    public boolean tryMoveTo(int dx, int dOrientation) {
        if (tetrade == null) {
            return false;
        }

        final TetrisCoordinate newCenter = TetrisCoordinate.of(tetradeCenter.x + dx, tetradeCenter.y);
        final int newOrientation = (tetradeOrientation + dOrientation) % tetrade.count();
        if (!canMoveTo(newCenter.x, newCenter.y, newOrientation)) {
            return false;
        }

        tetradeCenter = newCenter;
        tetradeOrientation = newOrientation;

        recomputeProjectedCoordinate();

        return true;
    }

    private void recomputeProjectedCoordinate() {
        // recompute projected tetrade center
        final int projectedX = tetradeCenter.x;
        int projectedY = tetradeCenter.y;
        while (projectedY >= 0 && canMoveTo(projectedX, projectedY, tetradeOrientation)) {
            projectedY--;
        }
        projectedTetradeY = projectedY + 1; // adjust Y-coordinate
    }

    @Nullable
    public List<TetrisCoordinate> getCurrentTetrade() {
        if (tetrade == null) {
            return null;
        }

        return cellOffsets(tetradeOrientation);
    }

    @Nullable
    public List<TetrisCoordinate> getNextTetrade() {
        if (nextTetrade == null) {
            return null;
        }
        return Tetrades.ORIENTATIONS.get(nextTetrade.getFirstOrientationIndex());
    }

    public int getProjectedTetradeY() {
        return projectedTetradeY;
    }

    public void save(SaveState state) {
        state.setCells(cells);
        state.setTetradeCenter(tetradeCenter);
        final List<Tetrades.OrientationIndices> indices = Arrays.asList(Tetrades.INDICES);
        state.setTetradeOffset(tetrade == null ? -1 : indices.indexOf(tetrade));
        state.setNextTetradeOffset(nextTetrade == null ? -1 : indices.indexOf(nextTetrade));
        state.setTetradeOrientation(tetradeOrientation);
    }

    public void load(SaveState state) {
        // copy cells
        final long[][] savedCells = state.getCells();
        for (int yCell = 0; yCell < cells.length; ++yCell) {
            System.arraycopy(savedCells[yCell], 0, cells[yCell], 0, DEFAULT_TETRIS_FIELD_WIDTH);
        }
        tetradeCenter = state.getTetradeCenter();
        tetrade = state.getTetradeOffset() >= 0 ? Tetrades.INDICES[state.getTetradeOffset()] : null;
        nextTetrade = state.getNextTetradeOffset() >= 0 ? Tetrades.INDICES[state.getNextTetradeOffset()] : null;
        tetradeOrientation = state.getTetradeOrientation();

        updateFieldListeners();
    }

    //
    // Private
    //

    private final long[][] cells = new long[DEFAULT_TETRIS_FIELD_HEIGHT][DEFAULT_TETRIS_FIELD_WIDTH];
    private TetrisCoordinate tetradeCenter = TetrisCoordinate.of(DEFAULT_SPAWNING_POINT.x, DEFAULT_SPAWNING_POINT.y);
    private int projectedTetradeY;
    private Tetrades.OrientationIndices tetrade;
    private Tetrades.OrientationIndices nextTetrade;
    private int tetradeOrientation;
    private final List<FieldChangeListener> fieldChangeListeners = new ArrayList<>();

    private List<Integer> checkLinesToClear(int minY, int maxY) {
        final List<Integer> result = new ArrayList<>();
        for (int i = minY; i <= maxY; ++i) {
            boolean clearThisLine = true;
            for (int x = 0; x < getFieldWidth(); ++x) {
                if (cells[i][x] == 0) {
                    clearThisLine = false;
                    break;
                }
            }
            if (clearThisLine) {
                result.add(i);
            }
        }
        return result;
    }

    private boolean canMoveTo(int tetradeCenterX, int tetradeCenterY, int tetradeOrientation) {
        final List<TetrisCoordinate> offsets = cellOffsets(tetradeOrientation);
        for (final TetrisCoordinate offset : offsets) {
            final int xCell = tetradeCenterX + offset.x;
            final int yCell = tetradeCenterY + offset.y;

            if (xCell < 0 || xCell >= getFieldWidth()) {
                return false;
            }

            if (yCell < 0 || yCell >= getFieldHeight()) {
                return false;
            }

            if (cells[yCell][xCell] != 0) {
                return false;
            }
        }
        return true;
    }

    private Tetrades.OrientationIndices generateNextTetrade() {
        final Tetrades.OrientationIndices result = nextTetrade;
        nextTetrade = Tetrades.INDICES[nextTetradeProvider.apply(Tetrades.INDICES.length)];
        return result != null ? result : generateNextTetrade();
    }

    private void clearLines(List<Integer> lines) {
        for (int i = lines.size() - 1; i >= 0; --i) {
            final int line = lines.get(i);
            for (int yScan = line + 1; yScan < getFieldHeight(); ++yScan) {
                System.arraycopy(cells[yScan], 0, cells[yScan - 1], 0, getFieldWidth());
            }
        }
    }

    private List<TetrisCoordinate> cellOffsets(int orientation) {
        if (tetrade == null || orientation < 0 || orientation >= tetrade.count()) {
            throw new IllegalStateException(String.format("unable to get cell offsets; tetrade=%s, orientation=%d", tetrade, orientation));
        }
        final int orientationPos = orientation + tetrade.getFirstOrientationIndex();
        return Tetrades.ORIENTATIONS.get(orientationPos);
    }
}
