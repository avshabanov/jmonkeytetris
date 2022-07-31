package io.shabanov.jmonkeytetris.model.save;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.shabanov.jmonkeytetris.model.TetrisCoordinate;
import io.shabanov.jmonkeytetris.model.save.SaveState;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class SaveStateTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void shouldSerializeSaveState() throws Exception {
        // Given:
        final SaveState initialState = new SaveState();
        initialState.setCells(new long[][]{{1, 2, 3}, {4, 5, 6}});
        initialState.setTetradeCenter(TetrisCoordinate.of(3, 7));

        // When:
        final String serialized = mapper.writeValueAsString(initialState);
        final SaveState recoveredState = mapper.readValue(serialized, SaveState.class);

        // Then:
        assertArrayEquals(initialState.getCells(), recoveredState.getCells());
        assertEquals(initialState.getTetradeCenter(), recoveredState.getTetradeCenter());
    }
}
