package it.unibo.aurea;

import it.unibo.aurea.controller.GameControllerImpl;
import it.unibo.aurea.controller.api.GameController;
import it.unibo.aurea.controller.api.PlayerInfo;
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
     * JavaFX entry point. Opens the login scene; game starts on submit.
     *
     * @param primaryStage the JavaFX-provided primary stage (unused)
     */
    @Override
    public void start(final Stage primaryStage) {
        openLogin();
    }

    /**
     * Opens the login scene. Called both on first launch and on restart.
     * Extracted so it can be referenced as a method reference (Runnable).
     */
    private void openLogin() {
        new LoginScene(this::startGame);
    }

    /**
     * Builds and starts the full MVC stack with the given player identity.
     * The view receives a restart callback that re-opens the login scene.
     *
     * @param playerInfo the identity collected at login
     */
    private void startGame(final PlayerInfo playerInfo) {
        try {
            final GameConfig config = GameConfigImpl.createStandard();
            final Deck deck = new Deck();
            final GameEngine engine = new GameEngineImpl(config, deck);

            final Runnable onRestart = () -> Platform.runLater(this::openLogin);

            final GameView view = new GameViewJavaFXImpl(onRestart);
            final GameController controller = new GameControllerImpl(view, engine, playerInfo);

            view.setController(controller);
            controller.startGame();
        } catch (final IllegalStateException e) {
            System.err.println("Errors in configuration of the environment: " + e.getMessage()); //NOPMD
            Platform.exit();
        }
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
