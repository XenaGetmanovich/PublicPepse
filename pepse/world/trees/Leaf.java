package pepse.world.trees;

import danogl.GameObject;
import danogl.collisions.Collision;
import danogl.collisions.GameObjectCollection;
import danogl.components.ScheduledTask;
import danogl.components.Transition;
import danogl.gui.mouse.MouseAction;
import danogl.gui.mouse.MouseActionParams;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.minigame.MemoryGameCard;
import pepse.minigame.MemoryGameManager;
import pepse.util.ColorSupplier;
import pepse.world.Block;

import java.awt.*;
import java.util.HashSet;
import java.util.Random;

/**
 * Responsible for the creation and management of leaves.
 * Each leaf can be used a memory game card.
 */
public class Leaf extends GameObject implements MemoryGameCard {
    private static final Color LEAF_COLOR = new Color(50, 200, 30);
    public static final float MOVEMENT_ANGLE = 3;
    private static final float LEAF_MOVEMENT_TRANSITION_TIME = 1;
    private static final float FALL_VELOCITY = 30;
    private static final float FADEOUT_TIME = 10;
    private static final float FADEIN_TIME = 1;
    private static final int MAX_DEATH_DURATION = 15;
    private static final int MAX_LIFE_DURATION = 50;
    private static final float MAX_X_VELOCITY = 50;
    private static final int MIN_LIFE_DURATION = 3;
    private static final float WIDTH_TRANSITION_TIME = 3;
    private static final float SHRUNKEN_LEAF_RATIO = (float) 9 / 10;
    private static final int LEAF_PROBABILITY_DENOMINATOR = 9;
    private static final int LEAF_PROBABILITY_NUMERATOR = 7;
    private static final int GAME_CARD_PROBABILITY_DENOMINATOR = 9;
    private static final int GAME_CARD_PROBABILITY_NUMERATOR = 5;
    private static final int MAX_MOVEMENT_DELAY = 100; // in centi-seconds
    private final Random random;
    public final Vector2 initialTopLeftCorner;
    private final GameObjectCollection gameObjects;
    private final HashSet<MemoryGameCard> revealedGameCards;
    private final Renderable renderable;
    public Renderable gameCardImg;
    private Transition<Float> horizontalTransition;
    private Transition<Float> angleTransition;
    private Transition<Vector2> widthTransition;
    private ScheduledTask fadeOutTask;
    private boolean didHitGround;
    private boolean isLeafAlive;

    /**
     * Constructor.
     *
     * @param topLeftCorner the coordinates of the top left corner of the leaf.
     * @param renderable The renderable representing the leaf.
     * @param gameObjects The collection of all participating game objects.
     * @param gameCardImg memory game card image.
     * @param revealedGameCards The cards that had already been revealed.
     */
    public Leaf(Vector2 topLeftCorner, Renderable renderable, GameObjectCollection gameObjects,
                Renderable gameCardImg, HashSet<MemoryGameCard> revealedGameCards) {
        super(topLeftCorner, Vector2.ONES.mult(Block.SIZE), renderable);
        this.initialTopLeftCorner = topLeftCorner;
        this.random = new Random();
        this.gameObjects = gameObjects;
        this.gameCardImg = gameCardImg;
        this.revealedGameCards = revealedGameCards;
        this.renderable = renderable;
        this.didHitGround = true;

        // Start the leafs life cycle
        life();
    }

    /**
     * This method creates a foliage (in which each leaf is a memory game card) in the given location with
     * the specified size.
     *
     * @param bottomLeftCorner the coordinates of the bottom left corner of the bottom left leaf of the
     *                         foliage.
     * @param foliageSize size of the foliage.
     * @param gameCardsPool All memory game card images.
     * @param gameObjects The collection of all participating game objects.
     * @param revealedGameCards The game cards that had already been revealed.
     * @param layer The object layer in which to create the leaves in.
     * @param leafRandomizer the Random object which will be used to determine where leaves will be created.
     */
    public static void createFoliage(Vector2 bottomLeftCorner, int foliageSize, Renderable[] gameCardsPool,
                                     GameObjectCollection gameObjects,
                                     HashSet<MemoryGameCard> revealedGameCards, int layer,
                                     Random leafRandomizer){
        Random cardSelector = new Random();

        for (int leafRow=1; leafRow<=foliageSize; leafRow++){
            for (int leafCol = 0; leafCol < foliageSize; leafCol++) {

                // Determine whether to create a leaf in this location or not
                if (leafRandomizer.nextInt(LEAF_PROBABILITY_DENOMINATOR) <= LEAF_PROBABILITY_NUMERATOR - 1) {
                    Renderable leafRenderable =
                            new RectangleRenderable(ColorSupplier.approximateColor(LEAF_COLOR));
                    Vector2 leafTopLeft = new Vector2(leafCol * Block.SIZE + bottomLeftCorner.x(),
                            bottomLeftCorner.y() - leafRow * Block.SIZE);

                    // Choose the image for the game card
                    Renderable gameCardImg = null;
                    if (new Random().nextInt(GAME_CARD_PROBABILITY_DENOMINATOR) <=
                            GAME_CARD_PROBABILITY_NUMERATOR - 1)
                        gameCardImg = gameCardsPool[cardSelector.nextInt(gameCardsPool.length)];

                    Leaf leaf = new Leaf(leafTopLeft, leafRenderable, gameObjects, gameCardImg,
                                         revealedGameCards);

                    gameObjects.addGameObject(leaf, layer);
                }
            }
        }
    }

    /*
     * The leafs living phase.
     * In this phase, the leaf is in its location (as part of a foliage), gently moves with the wind.
     */
    private void life() {
        isLeafAlive = true;

        if(!didHitGround){
            this.transform().setVelocity(Vector2.ZERO);
            this.didHitGround = true;
        }

        this.setTopLeftCorner(initialTopLeftCorner);
        this.renderer().fadeIn(FADEIN_TIME);
        float movementDelay = (float) random.nextInt(MAX_MOVEMENT_DELAY) / 100;

        // Angle change transition
        new ScheduledTask(this, movementDelay, false,
                () -> angleTransition = new Transition<Float>(this,
                        angle -> this.renderer().setRenderableAngle(angle),
                        -MOVEMENT_ANGLE, MOVEMENT_ANGLE,
                        Transition.LINEAR_INTERPOLATOR_FLOAT,
                        LEAF_MOVEMENT_TRANSITION_TIME,
                        Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                        null));

        // Width change transition
        new ScheduledTask(this, movementDelay,
                false, () -> widthTransition = new Transition<Vector2>(this,
                dimensionsAsVector2 -> this.setDimensions(dimensionsAsVector2),
                new Vector2(Block.SIZE * SHRUNKEN_LEAF_RATIO, Block.SIZE),
                new Vector2(Block.SIZE, Block.SIZE),
                Transition.LINEAR_INTERPOLATOR_VECTOR,
                LEAF_MOVEMENT_TRANSITION_TIME,
                Transition.TransitionType.TRANSITION_BACK_AND_FORTH,
                null));

        float lifeDuration = random.nextInt(MAX_LIFE_DURATION - MIN_LIFE_DURATION) + MIN_LIFE_DURATION
                            + movementDelay;
        fadeOutTask = new ScheduledTask(this, lifeDuration, false, this::fadeOut);
    }

    /*
     * The leafs fading out phase.
     * In this phase, the leaf is falling from the tree and slowly fading out.
     */
    private void fadeOut(){
        this.isLeafAlive = false;
        this.didHitGround = false;
        this.transform().setVelocityY(FALL_VELOCITY);

        if (horizontalTransition == null) {
            horizontalTransition = new Transition<Float>(this,
                    this.transform()::setVelocityX,
                    -MAX_X_VELOCITY, MAX_X_VELOCITY,
                    Transition.LINEAR_INTERPOLATOR_FLOAT, WIDTH_TRANSITION_TIME,
                    Transition.TransitionType.TRANSITION_BACK_AND_FORTH, null);
        }
        else {
            this.addComponent(horizontalTransition);
        }

        this.renderer().fadeOut(FADEOUT_TIME, this::death);
    }

    /*
     * The leafs death phase.
     * In this phase, the leaf is not shown on the screen, it waits to get "reborned" and start the living
     * phase again.
     */
    private void death(){
        if(!didHitGround){
            this.removeComponent(angleTransition);
            this.removeComponent(widthTransition);
            this.removeComponent(horizontalTransition);
        }

        float deathDuration = random.nextInt(MAX_DEATH_DURATION);
        new ScheduledTask(this, deathDuration, false, this::life);
    }


    /**
     * Handles mouse action.
     * On right mouse click - the leaf fades out.
     * On left mouse click - if there is a game card image attached: reveals/hides the image.
     *                       else: spins the leaf.
     *
     * @param params info regarding the mouse's parameters in this frame and the action that occurred.
     */
    @Override
    public void onMouseAction(MouseActionParams params) {
        super.onMouseAction(params);

        // Disables clicks when the leaf is not on the tree.
        if (!isLeafAlive){
            return;
        }

        if (params.getMouseAction() == MouseAction.BUTTON_UP) {
            this.removeComponent(fadeOutTask);

            switch (params.getButton()) {

                case RIGHT_BUTTON: // Make the leaf fall instantly
                    fadeOut();
                    break;

                case LEFT_BUTTON: // Reveals / Hides the game card image

                    // Allows only MAX_REVEALED_CARDS cards to be revealed at a time
                    if (revealedGameCards.size() >= MemoryGameManager.MAX_REVEALED_CARDS &&
                            !revealedGameCards.contains(this)){
                        break;

                    } else if (gameCardImg == null) {
                        this.removeComponent(angleTransition);
                        this.update(0);

                        // Spin leaf
                        new Transition<Float>(this,
                                angle -> this.renderer().setRenderableAngle(angle),
                                0f, 360f,
                                Transition.LINEAR_INTERPOLATOR_FLOAT,
                                1,
                                Transition.TransitionType.TRANSITION_ONCE,
                                () -> this.addComponent(angleTransition));
                        break;
                    }

                    if (!revealedGameCards.contains(this)) {
                        revealedGameCards.add(this);
                        reveal();
                    }
                    else {
                        turnOver();
                        revealedGameCards.remove(this);
                    }

                    break;
            }
        }
    }

    /**
     * Reveals the game card image (=displays it on screen).
     */
    @Override
    public void reveal() {
        this.removeComponent(angleTransition);
        this.removeComponent(widthTransition);
        super.update(0); // FLUSH CHANGES
        this.renderer().setRenderable(gameCardImg);
    }

    /**
     * Hides the game card image.
     */
    public void turnOver(){
        this.addComponent(angleTransition);
        this.addComponent(widthTransition);
        this.addComponent(fadeOutTask);
        this.renderer().setRenderable(renderable);
    }

    /**
     * Returns the memory game card image.
     *
     * @return memory game card image.
     */
    @Override
    public Renderable getCardImg() {
        return this.gameCardImg;
    }

    /**
     * This method is called by the MemoryGameManager when this card is revealed and matches another card
     * on screen.
     * This defines the game card behavior when there is a match - the card image gets hidden and the leaf
     * fades out.
     */
    @Override
    public void onMatch() {
        turnOver();
        fadeOut();
    }

    /**
     * When a leaf hits the ground, all of its movements are being deleted.
     *
     * @param other The GameObject with which a collision occurred.
     * @param collision Information regarding this collision.
     *                  A reasonable elastic behavior can be achieved with:
     *                  setVelocity(getVelocity().flipped(collision.getNormal()));
     */
    @Override
    public void onCollisionEnter(GameObject other, Collision collision) {
        super.onCollisionEnter(other, collision);

            this.removeComponent(angleTransition);
            this.removeComponent(widthTransition);
            this.removeComponent(horizontalTransition);
            super.update(0); // FLUSH CHANGES

            this.transform().setVelocity(Vector2.ZERO);
            this.didHitGround = true;
    }
}
