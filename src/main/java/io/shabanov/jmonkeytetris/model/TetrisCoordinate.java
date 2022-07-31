package io.shabanov.jmonkeytetris.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * Relative coordinates of a cell either in a bounding box or in a tetris field.
 *
 * A falling tetrade is fully determined by a 4-element list comprised of relative coordinates and a center
 * of a bounding box containing a tetrade.
 * <p>
 * See also {@link Tetrades#BOUNDING_BOX_RADIUS}.
 */
public class TetrisCoordinate {
    public final int x, y;

    private TetrisCoordinate(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @JsonCreator
    public static TetrisCoordinate of(@JsonProperty("x") int x, @JsonProperty("y") int y) {
        return new TetrisCoordinate(x, y);
    }

    public static TetrisCoordinate of(TetrisCoordinate other) {
        return of(other.x, other.y);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TetrisCoordinate that = (TetrisCoordinate) o;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
}
