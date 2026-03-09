package pepse.world;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.gui.ImageReader;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.ImageRenderable;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.util.FloatCounter;
import pepse.util.ImagesManager;

import java.awt.event.KeyEvent;
/**
 * An avatar can move around the world.
 */
public class Avatar extends GameObject{
    private static final int WALKING_SPEED = 200;
    private static final int GRAVITATION = 500;
    private static final int FLYING_SPEED = 300;
    private static final int JUMPING_SPEED = 400;

    //energy
    private static final int MAXIMUM_ENERGY = 100;
    private static final float ENERGY_LOSS_PER_FRAME = 0.5f;
    private static final float ENERGY_GAIN_PER_FRAME = 0.5f;
    private FloatCounter flyingEnergyCounter;

    private AvatarState lastVerticalState;
    private AvatarState lastHorizontalState;

    private final UserInputListener inputListener;

    //animations
    AnimationRenderable[] animations;
    static final double TIME_BETWEEN_CLIPS = 0.1;
    static final double TIME_BETWEEN_CLIPS_JUMP = 0.2;



    public Avatar(Vector2 topLeftCorner, Vector2 dimensions, Renderable avatarImage,
                  UserInputListener inputListener) {
        super(topLeftCorner, dimensions, avatarImage);
        this.inputListener = inputListener;
        lastHorizontalState = AvatarState.STAND;
        lastVerticalState = AvatarState.STAND;
        animations = new AnimationRenderable[AvatarState.values().length];
    }

    /**
     * Creates animation renderables for walking, jumping, flying and standing
     * @param avatar the avatar to add the animations to
     * @param imageReader will read the images of the goven files
     */
    public static void createAnimations(Avatar avatar, ImageReader imageReader){
        AnimationRenderable walkAnimation = new AnimationRenderable(ImagesManager.avatarWalkingFrames,
                                                                    imageReader, true,TIME_BETWEEN_CLIPS);
        AnimationRenderable flyingAnimation = new AnimationRenderable(ImagesManager.avatarFlyingFrames,
                                                            imageReader, true,TIME_BETWEEN_CLIPS);
        AnimationRenderable idleAnimation = new AnimationRenderable(ImagesManager.avatarStandingFrames,
                                                           imageReader, true,TIME_BETWEEN_CLIPS);
        AnimationRenderable jumpingAnimation = new AnimationRenderable(ImagesManager.avatarJumpingFrames,
                                                     imageReader, true, TIME_BETWEEN_CLIPS_JUMP);
        avatar.animations[AvatarState.WALK_LEFT.ordinal()] = walkAnimation;
        avatar.animations[AvatarState.WALK_RIGHT.ordinal()] = walkAnimation;
        avatar.animations[AvatarState.FLY.ordinal()] = flyingAnimation;
        avatar.animations[AvatarState.STAND.ordinal()] = idleAnimation;
        avatar.animations[AvatarState.FALL.ordinal()] = idleAnimation;
        avatar.animations[AvatarState.JUMP.ordinal()] = jumpingAnimation;
        avatar.renderer().setRenderable(walkAnimation);
    }

    /**
     * This function creates an avatar that can travel the world and is followed by the camera.
     * It can stand, walk, jump and fly, and never reaches the end of the world.
     *
     * @param gameObjects The collection of all participating game objects.
     * @param layer The number of the layer to which the created avatar should be added.
     * @param topLeftCorner The location of the top-left corner of the created avatar.
     * @param inputListener Used for reading input from the user.
     * @param imageReader Used for reading images from disk or from within a jar.
     *
     * @return A newly created representing the avatar.
     */
    public static Avatar create(GameObjectCollection gameObjects,
                                int layer, Vector2 topLeftCorner,
                                UserInputListener inputListener,
                                ImageReader imageReader) {
        ImageRenderable avatarImage = imageReader.readImage("assets/avatarMoves/idle/idle1.png", false);
        Vector2 size = new Vector2(avatarImage.width(), avatarImage.height()).mult(0.25f);
        Avatar adam = new Avatar(topLeftCorner, size, avatarImage, inputListener);
        gameObjects.addGameObject(adam, layer);
        adam.physics().preventIntersectionsFromDirection(Vector2.ZERO);
        adam.transform().setAccelerationY(GRAVITATION);
        createAnimations(adam, imageReader);
        return adam;
    }

    /**
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
        Vector2 newVelocity = this.getVelocity();
        AvatarState currHorizontalState = lastHorizontalState;
        AvatarState currVerticalState = lastVerticalState;

        //walk left
        if (inputListener.isKeyPressed(KeyEvent.VK_LEFT) && lastHorizontalState != AvatarState.WALK_LEFT) {
            newVelocity = newVelocity.add(new Vector2(Vector2.LEFT.mult(WALKING_SPEED).x(), 0));
            currHorizontalState = AvatarState.WALK_LEFT;
        }

        //walk right
        if (inputListener.isKeyPressed(KeyEvent.VK_RIGHT) && lastHorizontalState != AvatarState.WALK_RIGHT) {
            newVelocity = newVelocity.add(new Vector2(Vector2.RIGHT.mult(WALKING_SPEED).x(), 0));
            currHorizontalState = AvatarState.WALK_RIGHT;
        }
        //stop walking
        if (!inputListener.isKeyPressed(KeyEvent.VK_RIGHT) && !inputListener.isKeyPressed(KeyEvent.VK_LEFT)) {
            newVelocity = new Vector2(0, newVelocity.y());
            currHorizontalState = AvatarState.STAND;
        }
        //fly
        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && inputListener.isKeyPressed(KeyEvent.VK_SHIFT)
                && lastVerticalState != AvatarState.FLY && flyingEnergyCounter.value() != 0) {
            newVelocity = Vector2.UP.mult(FLYING_SPEED).add(new Vector2(newVelocity.x(), 0));
            this.transform().setAccelerationY(0);
            currVerticalState = AvatarState.FLY;
        }

        // stopped flying
        if(lastVerticalState == AvatarState.FLY &&
                (inputListener.wasKeyReleasedThisFrame(KeyEvent.VK_SPACE) ||
                inputListener.wasKeyReleasedThisFrame(KeyEvent.VK_SHIFT) ||
                        flyingEnergyCounter.value() == 0)){
            currVerticalState = AvatarState.FALL;
            this.transform().setAccelerationY(GRAVITATION);
        }

        //jump
        if (inputListener.isKeyPressed(KeyEvent.VK_SPACE) && isOnGround() &&
                lastVerticalState != AvatarState.JUMP) {
            newVelocity = newVelocity.add(Vector2.UP.mult(JUMPING_SPEED));
            currVerticalState = AvatarState.JUMP;
        }
        if (isOnGround() && lastVerticalState == AvatarState.JUMP) {
            currVerticalState = AvatarState.STAND;
        }


        updateFlyingEnergy(currVerticalState);
        this.setVelocity(newVelocity);
        lastVerticalState = currVerticalState;
        lastHorizontalState = currHorizontalState;
        System.out.println(currVerticalState + ", " +  currHorizontalState);
        setAnimation(currHorizontalState, currVerticalState);
    }

    /**
     * Receives a counter to update when energy level changes. The same counter
     * will be used to show the enrgy levels
     * @param counter the counter to update
     */
    public void setCounter(FloatCounter counter){
        this.flyingEnergyCounter = counter;
    }

    /**
     * sets the animation of the avatar according to it's moving state
     * @param currHorizontalState JUMP, FLY, STAND, FALL
     * @param currVerticalState WALK_RIGHT, WALK_LEFT, STAND
     */
    private void setAnimation(AvatarState currHorizontalState, AvatarState currVerticalState) {
             if (currVerticalState == AvatarState.FLY){
            this.renderer().setRenderable(animations[AvatarState.FLY.ordinal()]);
        }
        if (currVerticalState == AvatarState.JUMP){
            this.renderer().setRenderable(animations[AvatarState.JUMP.ordinal()]);
        }
        if (currVerticalState == AvatarState.FALL ||
                currVerticalState == AvatarState.STAND){
            this.renderer().setRenderable(animations[currHorizontalState.ordinal()]);
        }
        if(currHorizontalState == AvatarState.WALK_LEFT){
            this.renderer().setIsFlippedHorizontally(true);
        }
        else if (currHorizontalState == AvatarState.WALK_RIGHT)
        {
            this.renderer().setIsFlippedHorizontally(false);
        }

    }

    /**
     * Updates the energy reservoir of the avatar
     */
    private void updateFlyingEnergy(AvatarState currVerticalState) {

        if (currVerticalState == AvatarState.FLY && flyingEnergyCounter.value() != 0) {
            flyingEnergyCounter.decreaseBy(ENERGY_LOSS_PER_FRAME);
        }
        if (isOnGround() && flyingEnergyCounter.value() < MAXIMUM_ENERGY) {
            flyingEnergyCounter.increaseBy(ENERGY_GAIN_PER_FRAME);
        }
    }

    /**
     * chekcs if the avatar is on the ground
     * @return true if the avatar is on the ground, false otherwise
     */
    private boolean isOnGround() {
        return this.getVelocity().y() == 0 && lastVerticalState != AvatarState.FLY;
    }
}