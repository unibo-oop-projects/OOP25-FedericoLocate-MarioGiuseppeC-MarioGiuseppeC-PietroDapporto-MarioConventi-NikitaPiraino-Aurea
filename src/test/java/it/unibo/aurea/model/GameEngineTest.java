package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.GameConfig;
import it.unibo.aurea.model.api.GameEngine;
import it.unibo.aurea.model.api.GameState;
import it.unibo.aurea.model.api.ParameterType;

/**
 * Testing class for the GameEngine implementation.
 */
class GameEngineTest {

    private static final String PARENT_IMMEDIATE = "parent_immediate";
    private static final String CHILD_IMMEDIATE = "child_immediate";
    private static final String PARENT_DELAYED = "parent_delayed";
    private static final String CHILD_DELAYED = "child_delayed";
    private static final String TXT_APPROVE = "Approve";
    private static final String TXT_REFUSE = "Refuse";
    private static final String CARD_X_ID = "card_x";
    private static final int LIFE_POINTS = 200;

    private GameEngine engine;

    /**
     * Set up a new GameEngine before each test.
     */
    @BeforeEach
    void setUp() {
        final GameConfig config = GameConfigFactory.createStandard(it.unibo.aurea.model.api.Difficulty.EASY);
        this.engine = new GameEngineImpl(config, new GameClockImpl(config), new Deck());
    }

    /**
     * Tests if the game starts correctly in the RUNNING state.
     */
    @Test
    void testGameInitialization() {
        assertEquals(GameState.RUNNING, engine.getGameState(), "Game must start in RUNNING state");
        assertEquals(GameState.RUNNING, engine.getGameState(), "Game must remain RUNNING after starting");
    }

    /**
     * Tests if the game reaches the LOST state when a parameter's level hits the limit.
     */
    @Test
    void testGameLostOnParameterDeath() {
        assertEquals(GameState.RUNNING, engine.getGameState());

        // We simulate a loss by applying an effect that drains a parameter to 0,
        // going through the public applyEffects() API (parameters are now read-only outside the engine).
        final ParameterType firstParam = engine.getParameters().get(0).getName();
        engine.applyEffects(List.of(new EffectImpl(firstParam, -LIFE_POINTS)));

        assertEquals(GameState.LOST, engine.getGameState(), "Game must be LOST when a parameter hits 0");
    }

    /**
     * Tests if the game reaches the WON state when the clock finishes.
     */
    @Test
    void testGameWonOnTimeFinished() {
        assertEquals(GameState.RUNNING, engine.getGameState());

        // We simulate a win by advancing the clock until time is finished.
        // This validates the coordination between Engine and GameClock.
        while (!engine.getGameClock().isTimeFinished()) {
            engine.getGameClock().nextTurn();
        }

        assertEquals(GameState.WON, engine.getGameState(), "Game must be WON when time is finished");
    }

    /**
     * Tests that makeDecision properly applies effects, marks cards as used, and advances clock.
     */
    @Test
    void testMakeDecision() {
        final it.unibo.aurea.model.api.Card cardBefore = engine.getCurrentCard();
        assertNotNull(cardBefore, "A card must be active at the start");

        final int turnBefore = engine.getGameClock().getCurrentTurn();
        final int semesterBefore = engine.getGameClock().getCurrentSemester();

        // Make decision
        engine.makeDecision(true);

        // Clock must have advanced
        final int turnAfter = engine.getGameClock().getCurrentTurn();
        assertTrue(turnAfter > turnBefore || engine.getGameClock().getCurrentSemester() > semesterBefore,
            "The game clock must advance after a decision");

        // The card played must be marked as used
        assertTrue(cardBefore.isUsed(), "The played card must be marked as used");
    }

    @Test
    void testChildCardImmediateTrigger() {
        final TestDeck testDeck = new TestDeck();
        final int immediateDelay = 0;

        final it.unibo.aurea.model.api.Card parent = new CardImpl.Builder()
            .id(PARENT_IMMEDIATE)
            .character(it.unibo.aurea.model.api.CharacterType.PROFESSOR)
            .description("Parent")
            .textApproval(TXT_APPROVE)
            .textRefusal(TXT_REFUSE)
            .build();

        final it.unibo.aurea.model.api.Card child = new CardImpl.Builder()
            .id(CHILD_IMMEDIATE)
            .character(it.unibo.aurea.model.api.CharacterType.STUDENT)
            .description("Child")
            .textApproval(TXT_APPROVE)
            .textRefusal(TXT_REFUSE)
            .build();

        testDeck.setCards(List.of(parent));
        testDeck.setChildCards(List.of(child));
        testDeck.setFollowUps(List.of(
            new FollowUpImpl(PARENT_IMMEDIATE, CHILD_IMMEDIATE, it.unibo.aurea.model.api.OutcomeType.APPROVAL, immediateDelay)
        ));

        final GameConfig config = GameConfigFactory.createStandard(it.unibo.aurea.model.api.Difficulty.EASY);
        final GameEngine testEngine = new GameEngineImpl(
            config,
            new GameClockImpl(config),
            testDeck
        );

        // 1. Draw parent
        final it.unibo.aurea.model.api.Card firstCard = testEngine.getCurrentCard();
        assertEquals(PARENT_IMMEDIATE, firstCard.getId());

        // 2. Play APPROVAL -> this triggers follow-up with delay = 0
        testEngine.makeDecision(true);

        // 3. Draw child (since delay = 0, it must be the very next card)
        final it.unibo.aurea.model.api.Card secondCard = testEngine.getCurrentCard();
        assertEquals(CHILD_IMMEDIATE, secondCard.getId(), "Child card must be drawn immediately for delay=0");
    }

    @Test
    void testChildCardDelayedTrigger() {
        final TestDeck testDeck = new TestDeck();
        final int oneTurnDelay = 1;
        final int dummyDelay = 999;

        final it.unibo.aurea.model.api.Card parent = new CardImpl.Builder()
            .id(PARENT_DELAYED)
            .character(it.unibo.aurea.model.api.CharacterType.PROFESSOR)
            .description("Parent")
            .textApproval(TXT_APPROVE)
            .textRefusal(TXT_REFUSE)
            .build();

        final it.unibo.aurea.model.api.Card child = new CardImpl.Builder()
            .id(CHILD_DELAYED)
            .character(it.unibo.aurea.model.api.CharacterType.STUDENT)
            .description("Child")
            .textApproval(TXT_APPROVE)
            .textRefusal(TXT_REFUSE)
            .build();

        final it.unibo.aurea.model.api.Card cardX = new CardImpl.Builder()
            .id(CARD_X_ID)
            .character(it.unibo.aurea.model.api.CharacterType.PROFESSOR)
            .description("Other card")
            .textApproval(TXT_APPROVE)
            .textRefusal(TXT_REFUSE)
            .build();

        testDeck.setCards(List.of(parent, cardX));
        testDeck.setChildCards(List.of(child));
        testDeck.setFollowUps(List.of(
            new FollowUpImpl(PARENT_DELAYED, CHILD_DELAYED, it.unibo.aurea.model.api.OutcomeType.APPROVAL, oneTurnDelay),
            new FollowUpImpl("dummy_parent", CARD_X_ID, it.unibo.aurea.model.api.OutcomeType.APPROVAL, dummyDelay)
        ));

        final GameConfig config2 = GameConfigFactory.createStandard(it.unibo.aurea.model.api.Difficulty.EASY);
        final GameEngine testEngine = new GameEngineImpl(
            config2,
            new GameClockImpl(config2),
            testDeck
        );

        // 1. Draw parent
        final it.unibo.aurea.model.api.Card firstCard = testEngine.getCurrentCard();
        assertEquals(PARENT_DELAYED, firstCard.getId());

        // 2. Play APPROVAL -> triggers follow-up with delay = 1
        testEngine.makeDecision(true);

        // 3. Draw next card -> delay is 1, so the next card must be standard card (card_x)
        final it.unibo.aurea.model.api.Card secondCard = testEngine.getCurrentCard();
        assertEquals(CARD_X_ID, secondCard.getId(), "With delay=1, the immediate next card must be the other standard card");

        // 4. Play card_x
        testEngine.makeDecision(true);

        // 5. Draw third card -> since delay has reached 0, it must draw the child card
        final it.unibo.aurea.model.api.Card thirdCard = testEngine.getCurrentCard();
        assertEquals(CHILD_DELAYED, thirdCard.getId(), "After delay turns have passed, child card must be drawn");
    }

    private static final class TestDeck extends Deck {
        private final List<it.unibo.aurea.model.api.Card> cardsList = new ArrayList<>();
        private final List<it.unibo.aurea.model.api.Card> childrenList = new ArrayList<>();
        private final List<it.unibo.aurea.model.api.FollowUp> followUpsList = new ArrayList<>();

        public void setCards(final List<it.unibo.aurea.model.api.Card> cards) {
            this.cardsList.clear();
            this.cardsList.addAll(cards);
        }

        public void setChildCards(final List<it.unibo.aurea.model.api.Card> children) {
            this.childrenList.clear();
            this.childrenList.addAll(children);
        }

        public void setFollowUps(final List<it.unibo.aurea.model.api.FollowUp> followUps) {
            this.followUpsList.clear();
            this.followUpsList.addAll(followUps);
        }

        @Override
        public List<it.unibo.aurea.model.api.Card> getAllCards() {
            return List.copyOf(this.cardsList);
        }

        @Override
        public List<it.unibo.aurea.model.api.Card> getAllChildCards() {
            return List.copyOf(this.childrenList);
        }

        @Override
        public List<it.unibo.aurea.model.api.FollowUp> getAllFollowUps() {
            return List.copyOf(this.followUpsList);
        }
    }
}
