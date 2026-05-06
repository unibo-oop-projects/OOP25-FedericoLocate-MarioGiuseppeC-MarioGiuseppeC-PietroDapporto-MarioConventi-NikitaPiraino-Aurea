package it.unibo.aurea;

import it.unibo.aurea.controller.GameControllerImpl;
import it.unibo.aurea.controller.api.GameController;
import it.unibo.aurea.model.Deck;
import it.unibo.aurea.model.GameConfigImpl;
import it.unibo.aurea.model.GameEngineImpl;
import it.unibo.aurea.model.api.GameConfig;
import it.unibo.aurea.model.api.GameEngine;
import it.unibo.aurea.view.GameViewJavaFXImpl;
import it.unibo.aurea.view.LoginScene;
import it.unibo.aurea.view.api.GameView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 * This class is external from the MVC and is used only to start everything and creating the object model.
 * It isn't a static method inside the controller for respect the SRP principle.
 */
public final class Main extends Application {

    /**
     * JavaFX entry point. Called by the JavaFX runtime in the FX Application Thread.
     *
     * @param primaryStage the JavaFX-provided primary stage (unused: we open our own)
     */
    @Override
    public void start(final Stage primaryStage) {
        new LoginScene(playerInfo -> {
            try {
                final GameConfig config = GameConfigImpl.createStandard();
                final Deck deck = new Deck();

                final GameEngine engine = new GameEngineImpl(config, deck);
                final GameView view = new GameViewJavaFXImpl();
                final GameController controller = new GameControllerImpl(view, engine, playerInfo);

                view.setController(controller);
                controller.startGame();
            } catch (final IllegalStateException e) {
                System.err.println("Errors in configuration of the environment: " + e.getMessage()); //NOPMD
                Platform.exit();
            }
        });
    }

    /**
     * Standard Java entry point: hands off to the JavaFX runtime via {@code launch}.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        launch(args);
    }
}
