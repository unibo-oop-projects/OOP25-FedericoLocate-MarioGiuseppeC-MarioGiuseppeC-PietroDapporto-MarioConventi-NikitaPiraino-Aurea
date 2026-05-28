package it.unibo.aurea.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.CharacterType;
import it.unibo.aurea.model.api.Effect;
import it.unibo.aurea.model.api.ParameterType;

/**
 * Unit tests for CardImpl and Card.Builder.
 */
class CardImplTest {

    private static final int APPROVAL_FINANCES_DELTA = 10;
    private static final int APPROVAL_STUDENTS_DELTA = -5;
    private static final int APPROVAL_REPUTATION_DELTA = 20;
    private static final int REFUSAL_FINANCES_DELTA = -10;
    private static final int DECISION_FINANCES_DELTA = 5;

    @Test
    void testCardBuilderWithMultipleEffects() {
        final Card card = new CardImpl.Builder()
            .id("test_card")
            .character(CharacterType.PROFESSOR)
            .description("A test card description")
            .textApproval("Yes")
            .effectApproval(ParameterType.FINANCES, APPROVAL_FINANCES_DELTA)
            .effectApproval(ParameterType.STUDENTS, APPROVAL_STUDENTS_DELTA)
            .effectApproval(ParameterType.REPUTATION, APPROVAL_REPUTATION_DELTA)
            .textRefusal("No")
            .effectRefusal(ParameterType.FINANCES, REFUSAL_FINANCES_DELTA)
            .build();

        assertNotNull(card);
        assertEquals("test_card", card.getId());
        assertEquals(CharacterType.PROFESSOR, card.getCharacter());
        assertEquals("A test card description", card.getDescription());

        // Check approval effects (should have 3 effects)
        final List<Effect> approvalEffects = card.getApproval().getEffects();
        assertEquals(3, approvalEffects.size());
        assertEquals(ParameterType.FINANCES, approvalEffects.get(0).getParameter());
        assertEquals(APPROVAL_FINANCES_DELTA, approvalEffects.get(0).getDelta());
        assertEquals(ParameterType.STUDENTS, approvalEffects.get(1).getParameter());
        assertEquals(APPROVAL_STUDENTS_DELTA, approvalEffects.get(1).getDelta());
        assertEquals(ParameterType.REPUTATION, approvalEffects.get(2).getParameter());
        assertEquals(APPROVAL_REPUTATION_DELTA, approvalEffects.get(2).getDelta());

        // Check refusal effects (should have 1 effect)
        final List<Effect> refusalEffects = card.getRefusal().getEffects();
        assertEquals(1, refusalEffects.size());
        assertEquals(ParameterType.FINANCES, refusalEffects.get(0).getParameter());
        assertEquals(REFUSAL_FINANCES_DELTA, refusalEffects.get(0).getDelta());
    }

    @Test
    void testCardBuilderWithZeroEffects() {
        final Card card = new CardImpl.Builder()
            .id("zero_effects_card")
            .character(CharacterType.BUSINESSMAN)
            .description("Description")
            .textApproval("Approve")
            .textRefusal("Refuse")
            .build();

        assertNotNull(card);
        assertTrue(card.getApproval().getEffects().isEmpty(), "Approval effects list should be empty");
        assertTrue(card.getRefusal().getEffects().isEmpty(), "Refusal effects list should be empty");
    }

    @Test
    void testDecisionImmutability() {
        final List<Effect> effectsList = new ArrayList<>();
        effectsList.add(new EffectImpl(ParameterType.FINANCES, DECISION_FINANCES_DELTA));

        final Decision decision = new Decision("Answer", effectsList);
        assertEquals(1, decision.getEffects().size());

        // Mutate original list
        effectsList.add(new EffectImpl(ParameterType.STUDENTS, APPROVAL_FINANCES_DELTA));

        // Decision's internal list should NOT be modified
        assertEquals(1, decision.getEffects().size(), "Decision internal effects list must be immutable");
    }
}
