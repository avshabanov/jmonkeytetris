package io.shabanov.jmonkeytetris.model;

import io.shabanov.jmonkeytetris.util.PubSub;
import lombok.Value;
import lombok.experimental.UtilityClass;

@UtilityClass public class TetrisEvents {

    @Value(staticConstructor = "of") public class PhaseChanged implements PubSub.Event {
        TetrisGamePhase newPhase;
    }

    @Value(staticConstructor = "of") public class PhaseChangeRequest implements PubSub.Event {
        TetrisGamePhase requestedPhase;
    }
}
