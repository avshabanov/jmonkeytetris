package io.shabanov.jmonkeytetris.model;

import lombok.Value;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.Random;
import java.util.function.Function;

/**
 * Represents common information about tetrades used in the game.
 */
@UtilityClass public class Tetrades {

    /**
     * Designates fixed number of items in the above defined orientation array items (e.g. `0: T down` defines 4 elements, etc.)
     * This couldn't be a configurable parameter without also changing a bounding box model as well as logical model - e.g.
     * non-4-element figure wouldn't be called a tetrade anymore.
     */
    public static final int ELEMENTS_COUNT = 4;

    /**
     * Each tetrade above is supposed to fit a 5x5 box, 3 is the distance from the center of that box to either of
     * its edges. This number also means that above defined orientation element MUST ALWAYS be strictly less than this
     * value, e.g. 2 is the maximum for I-element.
     */
    public static final int BOUNDING_BOX_RADIUS = 3;

    /**
     * Captures each element with all its possible orientations, first position always points to a default
     * spawn orientation.
     *
     * This is tightly coupled with {@link #ORIENTATIONS} - each index is a position in the top-level orientation list.
     */
    public static final OrientationIndices[] INDICES = {
        OrientationIndices.of(0, 4),   // T-element
        OrientationIndices.of(4, 8),   // J
        OrientationIndices.of(8, 10),  // Z
        OrientationIndices.of(10, 11), // O
        OrientationIndices.of(11, 13), // S
        OrientationIndices.of(13, 17), // L
        OrientationIndices.of(17, 19), // I
    };

    /**
     * Encodes information about every tetrade and its coordinates depending on its orientation.
     *
     * Reordering this as well as removing or adding new tetrade entries MAY break {@link #INDICES} so these
     * two should be always updated together.
     */
    public static final List<List<TetrisCoordinate>> ORIENTATIONS = List.of(
            List.of(TetrisCoordinate.of(-1,  0), TetrisCoordinate.of(0,  0), TetrisCoordinate.of(1,  0), TetrisCoordinate.of(0,  1)),     //  0: T down (spawn)
            List.of(TetrisCoordinate.of( 0, -1), TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 0,  1)),  //  1: T left
            List.of(TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0), TetrisCoordinate.of( 0, -1)),  //  2: T up
            List.of(TetrisCoordinate.of( 0, -1), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0), TetrisCoordinate.of( 0,  1)),  //  3: T right

            List.of(TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0), TetrisCoordinate.of( 1,  1)),  //  4: J down (spawn)
            List.of(TetrisCoordinate.of( 0, -1), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of(-1,  1), TetrisCoordinate.of( 0,  1)),  //  5: J left
            List.of(TetrisCoordinate.of(-1, -1), TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0)),  //  6: J up
            List.of(TetrisCoordinate.of( 0, -1), TetrisCoordinate.of( 1, -1), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 0,  1)),  //  7: J right

            List.of(TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 0,  1), TetrisCoordinate.of( 1,  1)),  //  8: Z horizontal (spawn)
            List.of(TetrisCoordinate.of( 1, -1), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0), TetrisCoordinate.of( 0,  1)),  //  9: Z vertical

            List.of(TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of(-1,  1), TetrisCoordinate.of( 0,  1)),  // 10: O (spawn)

            List.of(TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0), TetrisCoordinate.of(-1,  1), TetrisCoordinate.of( 0,  1)),  // 11: S horizontal (spawn)
            List.of(TetrisCoordinate.of( 0, -1), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0), TetrisCoordinate.of( 1,  1)),  // 12: S vertical

            List.of(TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0), TetrisCoordinate.of(-1,  1)),  // 13: L down (spawn)
            List.of(TetrisCoordinate.of(-1, -1), TetrisCoordinate.of( 0, -1), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 0,  1)),  // 14: L left
            List.of(TetrisCoordinate.of( 1, -1), TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0)),  // 15: L up
            List.of(TetrisCoordinate.of( 0, -1), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 0,  1), TetrisCoordinate.of( 1,  1)),  // 16: L right

            List.of(TetrisCoordinate.of(-2,  0), TetrisCoordinate.of(-1,  0), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 1,  0)),  // 17: I horizontal (spawn)
            List.of(TetrisCoordinate.of( 0, -2), TetrisCoordinate.of( 0, -1), TetrisCoordinate.of( 0,  0), TetrisCoordinate.of( 0,  1))   // 18: I vertical
    );

    public static Function<Integer, Integer> tetradeProviderFromRandom(Random random) {
        return random::nextInt;
    }

    @Value(staticConstructor = "of")
    public static class OrientationIndices {
        int firstOrientationIndex;
        int lastOrientationIndex;

        int count() { return lastOrientationIndex - firstOrientationIndex; }
    }
}
