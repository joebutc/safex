package app;

import app.model.State;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StateFxmlResourceTest {

    @Test
    void everyStatePointsToAnExistingFxmlResource() {
        for (State state : State.values()) {
            assertNotNull(
                    AppController.class.getResource(state.getFxmlPath()),
                    () -> "Missing FXML resource for " + state + ": " + state.getFxmlPath());
        }
    }
}
