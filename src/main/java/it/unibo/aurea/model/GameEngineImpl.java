package it.unibo.aurea.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.Effect;
import it.unibo.aurea.model.api.FollowUp;
import it.unibo.aurea.model.api.GameClock;
import it.unibo.aurea.model.api.GameConfig;
import it.unibo.aurea.model.api.GameEngine;
import it.unibo.aurea.model.api.GameState;
import it.unibo.aurea.model.api.Parameter;
import it.unibo.aurea.model.api.ParameterType;

/**
 * this is the main implementation of the model.
 */
public final class GameEngineImpl implements GameEngine {

    private final Deck deck;
    private final GameConfig config;
    private final GameClock gameClock;
    private final Random randomGenerator;
    
    // La nostra Event Queue per gestire le carte figlie
    private final List<ActiveFollowUp> eventQueue = new ArrayList<>();
    
    // Variabile per evitare di ricalcolare l'estrazione più volte nello stesso turno
    private Card currentCardToPlay;

    private final List<Parameter> parameters = List.of(
        new ParameterImpl(ParameterType.FINANCES),
        new ParameterImpl(ParameterType.STUDENTS),
        new ParameterImpl(ParameterType.PROFESSORS),
        new ParameterImpl(ParameterType.REPUTATION)
    );

    /**
     * @param config is an object of the @code GameConfiguration.java .
     * @param deck contains the deck of card that wiil be used in future.
     */
    public GameEngineImpl(final GameConfig config, final Deck deck) {
        this.config = config;
        this.gameClock = new GameClockImpl(config);
        this.deck = Objects.requireNonNull(deck, "Deck cannot be null");
        this.randomGenerator = new Random();
        this.currentCardToPlay = extractNextCard();
    }

    @Override
    public GameConfig getGameConfig() {
        return config;
    }

    @Override
    public boolean isTimeFinished() {
        return gameClock.isTimeFinished();
    }

    @Override
    public void start() {
        // Avvio standard
    }

    @Override
    public Card getCurrentCard() {
        // Se la carta è stata usata nel turno precedente, ne pesca una nuova
        if (this.currentCardToPlay == null || this.currentCardToPlay.isUsed()) {
            this.currentCardToPlay = extractNextCard();
        }
        return this.currentCardToPlay;
    }
    
    /**
     * Algoritmo Centrale di Selezione Carte (Il tuo algoritmo tradotto)
     */
    private Card extractNextCard() {
        // 1. GESTIONE EVENTI CONCATENATI (Code)
        updateEventQueue();
        
        for (ActiveFollowUp activeEvent : eventQueue) {
            if (activeEvent.getRemainingTurns() <= 0) {
                // Troviamo la carta figlia associata all'evento
                Card forcedCard = deck.getAllCards().stream()
                    .filter(c -> c.getId().equals(activeEvent.getFollowUp().getChildId()))
                    .findFirst()
                    .orElse(null);
                    
                if (forcedCard != null && !forcedCard.isUsed()) {
                    eventQueue.remove(activeEvent);
                    return forcedCard; // ESTRAZIONE FORZATA (ignora salvavita)
                }
            }
        }

        // 2. PESI DINAMICI E FILTRO SALVAVITA
        ParameterType criticalParam = ParameterType.FINANCES;
        int minDistance = 50;

        // Trova il parametro più vicino alla morte (0 o 100)
        for (Parameter p : parameters) {
            int dist0 = p.getLevel();
            int dist100 = 100 - p.getLevel();
            int currentMinDist = Math.min(dist0, dist100);
            
            if (currentMinDist < minDistance) {
                minDistance = currentMinDist;
                criticalParam = p.getName();
            }
        }

        List<Card> playableCards = new ArrayList<>();
        List<Double> weights = new ArrayList<>();
        double totalWeight = 0.0;

        // Analisi carte giocabili (filtro Anti-Game Over matematico)
        for (Card c : deck.getAllCards()) {
            if (!c.isUsed() && isBaseCard(c.getId())) { 
                
                if (!isLethalInBothOptions(c)) {
                    playableCards.add(c);
                    
                    // Calcolo del peso
                    double weight = 10.0;
                    if (cardHelpsParameter(c, criticalParam)) {
                        weight *= (1.0 + (50.0 - minDistance) / 25.0);
                    }
                    weights.add(weight);
                    totalWeight += weight;
                }
            }
        }

        // Fallback: se tutto è letale o non ci sono carte
        if (playableCards.isEmpty()) {
            return deck.getAllCards().stream().filter(c -> !c.isUsed()).findFirst().orElse(deck.getAllCards().get(0));
        }

        // Estrazione pesata
        double randomVal = randomGenerator.nextDouble() * totalWeight;
        double currentSum = 0;
        
        for (int i = 0; i < playableCards.size(); i++) {
            currentSum += weights.get(i);
            if (randomVal <= currentSum) {
                return playableCards.get(i);
            }
        }
        
        return playableCards.get(0);
    }
    
    // --- METODI DI SUPPORTO PER L'ALGORITMO ---

    private boolean isBaseCard(String id) {
        // Se le carte figlie hanno ID che iniziano con "EV" o sono numeri > 100
        // Per semplicità, consideriamo base quelle che non sono mai 'figlie' in nessun follow up.
        return deck.getAllFollowUps().stream().noneMatch(fu -> fu.getChildId().equals(id));
    }

    private boolean isLethalInBothOptions(Card card) {
        boolean yesIsLethal = simulateLethality(card.getApproval().getEffects());
        boolean noIsLethal = simulateLethality(card.getRefusal().getEffects());
        return yesIsLethal && noIsLethal;
    }

    private boolean simulateLethality(List<Effect> effects) {
        for (Effect e : effects) {
            for (Parameter p : parameters) {
                if (p.getName() == e.getParameter()) {
                    int futureValue = p.getLevel() + e.getDelta();
                    if (futureValue <= 0 || futureValue >= 100) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean cardHelpsParameter(Card card, ParameterType type) {
        // Controlla se la carta ha un effetto positivo su quel parametro
        return card.getAllEffects().stream()
            .anyMatch(e -> e.getParameter() == type && e.getDelta() > 0);
    }

    private void updateEventQueue() {
        Iterator<ActiveFollowUp> iterator = eventQueue.iterator();
        while (iterator.hasNext()) {
            ActiveFollowUp event = iterator.next();
            event.decrementTurn();
        }
    }
    
    /**
     * Da richiamare nel Controller dopo ogni scelta, o qui intercettando la decisione.
     * Per ora la lasciamo pubblica così il Controller può inserire in coda.
     */
    public void registerChoiceConsequences(String parentId, boolean wasApproval) {
         // I tuoi colleghi hanno definito OutcomeType. Supponiamo ci sia ACCEPTED o REFUSED.
         // Dobbiamo tradurlo in base a come l'hanno scritto loro. (Es: OutcomeType.APPROVAL)
         deck.getAllFollowUps().stream()
            .filter(fu -> fu.getParentId().equals(parentId))
            .forEach(fu -> {
                // Semplificazione: Inseriamo il controllo corretto dell'enum dopo
                // eventQueue.add(new ActiveFollowUp(fu, fu.getDelayTurn()));
            });
    }

    @Override
    public List<Parameter> getParameters() {
        return this.parameters;
    }

    @Override
    public List<Parameter> getCopyOfParameters() {
        return List.copyOf(parameters);
    }

    @Override
    public GameState getGameState() {
        if (!areAllParametersAlive()) { 
            return GameState.LOST;
        }
        if (gameClock.isTimeFinished()) {
            return GameState.WON;
        }
        return GameState.RUNNING;
    }

    private boolean areAllParametersAlive() {
        return parameters.stream().allMatch(Parameter::isAlive);
    }

    @Override
    public GameClock getGameClock() {
        return this.gameClock;
    }
    
    // --- CLASSE INTERNA PER LA GESTIONE DEL TIMER DEGLI EVENTI ---
    private static class ActiveFollowUp {
        private final FollowUp followUp;
        private int remainingTurns;

        public ActiveFollowUp(FollowUp followUp, int remainingTurns) {
            this.followUp = followUp;
            this.remainingTurns = remainingTurns;
        }

        public void decrementTurn() {
            this.remainingTurns--;
        }

        public int getRemainingTurns() {
            return remainingTurns;
        }

        public FollowUp getFollowUp() {
            return followUp;
        }
    }
}