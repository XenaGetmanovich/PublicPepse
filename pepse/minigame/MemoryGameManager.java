package pepse.minigame;

import danogl.GameObject;
import danogl.components.ScheduledTask;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

import java.util.HashSet;
import java.util.Iterator;

/**
 * Manages a memory game card.
 * Responsible for checking for matches between revealed cards, and showing an animation when a match is found
 *
 * @see MemoryGameCard
 */
public class MemoryGameManager extends GameObject {
    public static final int MAX_REVEALED_CARDS = 2;
    public final HashSet<MemoryGameCard> revealedCards;
    private final AnimationRenderable onMatchAnimation;

    /**
     * Constructor.
     *
     * @param center center location of the on-match animation.
     * @param dimensions dimensions of the on-match animation.
     * @param onMatchAnimation the animation being displayed when there is a match.
     */
    public MemoryGameManager(Vector2 center, Vector2 dimensions, AnimationRenderable onMatchAnimation){
        super(Vector2.ZERO, dimensions, null);
        this.setCenter(center);
        this.revealedCards = new HashSet<>();
        this.onMatchAnimation = onMatchAnimation;
    }

    /**
     * Checks for matches between revealed cards.
     *
     * @param deltaTime The time elapsed, in seconds, since the last frame. Can
     *                  be used to determine a new position/velocity by multiplying
     *                  this delta with the velocity/acceleration respectively
     *                  and adding to the position/velocity:
     *                  velocity += deltaTime*acceleration
     *                  pos += deltaTime*velocity
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);

        if (this.revealedCards.size() == MAX_REVEALED_CARDS){
            Runnable func;

            if (checkForMatches()){
                func = this::onMatch;
            }
            else {
                func = this::turnOverLeaves;
            }

            new ScheduledTask(this, 1, false, func);
        }
    }

    /*
     * This function is being called when there is a match.
     * Goes over all revealed cards, executes their onMatch function and removes them from the revealed cards'
     * collection.
     * Activates the onMatch animation.
     */
    private void onMatch() {
        for (MemoryGameCard cardObj: revealedCards){
            cardObj.onMatch();
        }

        revealedCards.clear();
        this.renderer().setRenderable(onMatchAnimation);
        new ScheduledTask(this, 5, false, () -> renderer().setRenderable(null));
    }

    /*
     * Returns whether there is a match among the revealed game cards.
     */
    private boolean checkForMatches(){
        Iterator<MemoryGameCard> iterator = revealedCards.iterator();
        Renderable cardImg = null;

        while (iterator.hasNext()){
            if (cardImg == null){
                cardImg = iterator.next().getCardImg();
            }
            else if (iterator.next().getCardImg() != cardImg){
                return false;
            }
        }

        return true;
    }

    /*
     * Turns over all revealed game cards and clears the revealed cards' collection.
     */
    private void turnOverLeaves(){
        for (MemoryGameCard card: revealedCards){
            card.turnOver();
        }

        revealedCards.clear();
    }
}
