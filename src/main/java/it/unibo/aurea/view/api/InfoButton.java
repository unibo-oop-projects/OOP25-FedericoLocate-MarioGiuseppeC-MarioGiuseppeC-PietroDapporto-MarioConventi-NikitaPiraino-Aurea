package it.unibo.aurea.view.api;

import javafx.scene.control.Button;

/**
 * this interface manages the infoButton.
 */
@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface InfoButton {

    /**
     * Builds and returns the info button.
     *
     * @return the configured {@link Button}
     */
    Button build();

}
