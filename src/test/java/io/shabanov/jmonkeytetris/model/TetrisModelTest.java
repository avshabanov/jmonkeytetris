package io.shabanov.jmonkeytetris.model;

import io.shabanov.jmonkeytetris.model.Tetrades;
import io.shabanov.jmonkeytetris.model.TetrisModel;
import io.shabanov.jmonkeytetris.util.PubSub;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class TetrisModelTest {
    @Test
    void shouldSpawnNewTetrade() {
        final TetrisModel model = new TetrisModel(PubSub.noOpManager(), Tetrades.tetradeProviderFromRandom(ThreadLocalRandom.current()));
        assertTrue(model.spawnNewTetrade());
    }

    @Test
    void shouldClearTwoLines() {
        // Given:
        final Function<Integer, Integer> tetradeProvider = (n) -> 3; // O-element
        final TetrisModel model = new TetrisModel(PubSub.noOpManager(), tetradeProvider);

        // When: (fill two lines with O-elements)
        final List<Integer> dxOffsets = List.of(-4, -2, 0, 2);
        TetrisModel.MoveResult result;
        for (final int dx : dxOffsets) {
            result = spawnAndFallDown(model, dx);
            assertTrue(result.getClearedLines().isEmpty(), "dx=" + dx + " block should yield no cleared lines");
        }

        // Then: (dropping last O-element should result in clearing first two lines)
        result = spawnAndFallDown(model, 4);
        assertEquals(List.of(0, 1), result.getClearedLines(), "fifth block should yield two cleared lines");
        // also ensure the field is completely clear as a result of this
        for (int x = 0; x < model.getFieldWidth(); ++x) {
            for (int y = 0; y < model.getFieldHeight(); ++y) {
                assertEquals(0, model.getFieldAt(x, y), String.format("field at %dx%d", x, y));
            }
        }
    }

    private TetrisModel.MoveResult spawnAndFallDown(TetrisModel model, int dx) {
        assertTrue(model.spawnNewTetrade());
        model.tryMoveTo(dx, 0);
        TetrisModel.MoveResult result = model.moveDown();
        while (!result.isLastMove()) {
            result = model.moveDown();
        }
        return result;
    }
}
