package pepse.minigame;

import danogl.gui.rendering.Renderable;

/**
 * Represents a memory game card.
 *
 * @see MemoryGameManager
 */
public interface MemoryGameCard {
    /**
     * Reveals the game card image to the user.
     */
    void reveal();

    /**
     * Hides the game card image from the user.
     */
    void turnOver();

    /**
     * Returns the renderable that represents the game card image.
     *
     * @return memory game card image.
     */
    Renderable getCardImg();

    /**
     * This method is called by the MemoryGameManager when this card is revealed and matches another
     * revealed card.
     * It defines the game card behavior when there is a match.
     */
    void onMatch();
}
