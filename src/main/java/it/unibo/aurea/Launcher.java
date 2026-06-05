package it.unibo.aurea;

/**
 * Punto di ingresso che non estende {@link javafx.application.Application}.
 * È necessario per lanciare l'applicazione da un JAR eseguibile: quando la
 * main class estende Application, il runtime JavaFX si rifiuta di partire se
 * i moduli non sono sul module-path. Delegando a {@link Main}, JavaFX viene
 * inizializzato dal classpath e il JAR diventa eseguibile su tutte le piattaforme.
 */
public final class Launcher {

    private Launcher() { }

    /**
     * Standard Java entry point. Delegates to {@link Main}.
     *
     * @param args command-line arguments
     */
    public static void main(final String[] args) {
        Main.main(args);
    }
}
