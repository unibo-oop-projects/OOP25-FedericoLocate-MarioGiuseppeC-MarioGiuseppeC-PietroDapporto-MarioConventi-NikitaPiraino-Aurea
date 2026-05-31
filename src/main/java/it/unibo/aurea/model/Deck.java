package it.unibo.aurea.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import it.unibo.aurea.model.api.Card;
import it.unibo.aurea.model.api.FollowUp;
import it.unibo.aurea.model.dto.CardDTO;
import it.unibo.aurea.model.dto.CardsFile;
import it.unibo.aurea.model.dto.EffectDTO;
import it.unibo.aurea.model.dto.FollowUpDTO;
import it.unibo.aurea.model.dto.FollowUpsFile;

/**
 * It represents the container of all the cards of the game. 
 */
public class Deck {
    private final List<Card> cardsDeck = new ArrayList<>();
    private final List<FollowUp> followUps = new ArrayList<>();
    private final List<Card> childCards = new ArrayList<>();

    /**
     * Constructor of the deck from a .yaml file. 
     * 
     * @throws IOException if the reading from the file doesn't work
     */
    public Deck() {
        final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        this.cardsDeck.addAll(this.loadCards(mapper, "cards.yaml"));
        this.childCards.addAll(this.loadCards(mapper, "childCards.yaml"));
        this.followUps.addAll(this.loadFollowUp(mapper, "followups.yaml"));
    }

    /**
     * Loads and converts card definitions from the given YAML resource file.
     *
     * @param mapper the object mapper used to deserialize the YAML file
     * @param fileName the name of the YAML resource file to load
     * @return the list of loaded cards
     */
    private List<Card> loadCards(final ObjectMapper mapper, final String fileName) {
        final InputStream input = Deck.class.getClassLoader().getResourceAsStream(fileName);
        if (Objects.isNull(input)) {
            throw new IllegalStateException("Unable to find" + fileName);
        }
        try {
            final CardsFile cardFile = mapper.readValue(input, CardsFile.class);
            return cardFile.cards().stream().map(this::toCard).toList();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to load" + fileName + ":" + e.getMessage(), e);
        }
    }

    /**
     * Loads and converts follow-up rules from the given YAML resource file.
     *
     * @param mapper the object mapper used to deserialize the YAML file
     * @param fileName the name of the YAML resource file to load
     * @return the list of loaded follow-up rules
     */
    private List<FollowUp> loadFollowUp(final ObjectMapper mapper, final String fileName) {
        final InputStream input = Deck.class.getClassLoader().getResourceAsStream(fileName);
        if (Objects.isNull(input)) {
            throw new IllegalStateException("Unable to find" + fileName);
        }
        try {
            final FollowUpsFile followUpFile = mapper.readValue(input, FollowUpsFile.class);
            return followUpFile.followups().stream().map(this::toFollowUp).toList();
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to load" + fileName + ":" + e.getMessage(), e);
        }
    }

    /**
     * It gets all the card in the deck. 
     * 
     * @return a {@code List} of cards
     */
    public List<Card> getAllCards() {
        return List.copyOf(this.cardsDeck);
    }

    /**
     * It gets all the child cards. 
     * 
     * @return a {@code List} of cards
     */
    public List<Card> getAllChildCards() {
        return List.copyOf(this.childCards);
    }

    /**
     * @return a copy list of all the follow-up
     */
    public List<FollowUp> getAllFollowUps() {
        return List.copyOf(this.followUps);
    }

    /**
     * Private method that converts an istance of {@code CardDTO} in a {@code Card}.
     * 
     * @param cardDto the element to convert
     * @return a {@code Card} with the same characteristics of the input
     */
    private Card toCard(final CardDTO cardDto) {
        final var res = new CardImpl.Builder()
            .id(cardDto.id())
            .character(cardDto.character())
            .description(cardDto.description())
            .textRefusal(cardDto.refusal().text())
            .textApproval(cardDto.approval().text());

            for (final EffectDTO e : cardDto.refusal().effects()) {
                res.effectRefusal(e.parameter(), e.delta());
            }

            for (final EffectDTO e : cardDto.approval().effects()) {
                res.effectApproval(e.parameter(), e.delta());
            }

        return res.build();
    }

    /**
     * Private method that converts an istance of {@code FollowUpDTO} in a {@code FollowUpImpl}.
     * 
     * @param fuDTO the input element
     * @return the correct output
     */
    private FollowUp toFollowUp(final FollowUpDTO fuDTO) {
        return new FollowUpImpl(fuDTO.parentId(), fuDTO.childId(), fuDTO.trigger(), fuDTO.delayTurn());
    }

}
