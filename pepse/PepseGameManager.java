package pepse;

import danogl.GameManager;
import danogl.GameObject;
import danogl.collisions.Layer;
import danogl.components.CoordinateSpace;
import danogl.gui.*;
import danogl.gui.rendering.AnimationRenderable;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.Camera;
import danogl.util.Vector2;
import pepse.minigame.MemoryGameManager;
import pepse.util.ImagesManager;
import pepse.world.Block;
import pepse.util.FloatCounter;
import pepse.world.EnergyCounter.NumericEnergyCounter;
import pepse.world.Sky;
import pepse.world.Terrain;
import pepse.world.daynight.Night;
import pepse.world.daynight.Sun;
import pepse.world.daynight.SunHalo;
import pepse.world.trees.Tree;
import pepse.world.Avatar;

import java.awt.*;
import java.util.Random;

/**
 * The main class of the simulator.
 */
public class PepseGameManager extends GameManager {
    private static final String WINDOW_TITLE = "Pepse";
    private static final Vector2 DEFAULT_WINDOW_DIMENSION = new Vector2(990, 720);
    private static final Color SUN_HALO_COLOR = new Color(255, 255, 0, 20);

    // Layers
    private static final int TERRAIN_LAYER = Layer.STATIC_OBJECTS;
    private static final int AVATAR_LAYER = Layer.DEFAULT;
    private static final int SUN_LAYER = Layer.BACKGROUND + 1;
    private static final int SKY_LAYER = Layer.BACKGROUND;
    private static final int NIGHT_LAYER = Layer.FOREGROUND;
    private static final int SUN_HALO_LAYER = Layer.BACKGROUND + 10;
    private static final int ENERGY_COUNTER_LAYER = Layer.FOREGROUND - 10;
    private static final int TREES_LAYER = Layer.STATIC_OBJECTS + 1;
    private static final int LEAVES_LAYER = TREES_LAYER + 1;
    private static final int MAXIMUM_ENERGY = 100;
    private static final String BACKGROUND_MUSIC = "assets/backgroundMusic.wav";
    private Vector2 ENERGY_COUNTER_SIZE = new Vector2(25,25);
    private String ENERGY_COUNTER_TEXT = "Energy Level: ";


    //the layers to delete when deleting chunks
    private final int[] layersToDelete;

    //chunks
    private float[] mainChunk;
    private float[] rightChunk;
    private float[] leftChunk;
    private float CHUNK_SIZE;
    private float CHUNK_MARGIN;
    private static final int RANGE_COORDINATES = 2;
    private static final int FROM = 0;
    private static final int TO = 1;


    //fields
    private static final float SUN_CYCLE_LENGTH = 50;
    private static final float NIGHT_CYCLE_LENGTH = SUN_CYCLE_LENGTH / 2;
    private Avatar mainAvatar;
    private Terrain terrain;
    private Tree tree;
    private MemoryGameManager memoryGameManager;

    public PepseGameManager() {
        super(WINDOW_TITLE, DEFAULT_WINDOW_DIMENSION);
        layersToDelete = new int[]{TREES_LAYER, LEAVES_LAYER, TERRAIN_LAYER};
        mainChunk = new float[RANGE_COORDINATES];
    }

    /**
     * The method will be called once when a GameGUIComponent is created, and again after every invocation o
     * windowController.resetGame().
     *
     * @param imageReader Contains a single method: readImage, which reads an image from disk.
     *                 See its documentation for help.
     * @param soundReader Contains a single method: readSound, which reads a wav file from
     *                    disk. See its documentation for help.
     * @param inputListener Contains a single method: isKeyPressed, which returns whether
     *                      a given key is currently pressed by the user or not. See its
     *                      documentation.
     * @param windowController Contains an array of helpful, self-explanatory methods
     *                         concerning the window.
     */
    @Override
    public void initializeGame(ImageReader imageReader, SoundReader soundReader,
                               UserInputListener inputListener, WindowController windowController) {
        super.initializeGame(imageReader, soundReader, inputListener, windowController);

        int seed = new Random().nextInt();
        CHUNK_SIZE = windowController.getWindowDimensions().x() * 2f;
        CHUNK_MARGIN = CHUNK_SIZE / 4;
        mainChunk[FROM] = windowController.getWindowDimensions().x() - CHUNK_SIZE * 1/2;
        mainChunk[TO] = windowController.getWindowDimensions().x() + CHUNK_SIZE * 1/2;

        // Sets background music
        Sound backgroundMusic = soundReader.readSound(BACKGROUND_MUSIC);
        backgroundMusic.playLooped();

        // Create Sky
        Sky.create(this.gameObjects(), windowController.getWindowDimensions(), SKY_LAYER);

        // Create Terrain
        this.terrain = new Terrain(gameObjects(), TERRAIN_LAYER, DEFAULT_WINDOW_DIMENSION, seed);
        terrain.createInRange((int) mainChunk[FROM], (int) mainChunk[TO]);

        // Create Night
        Night.create(gameObjects(), NIGHT_LAYER, windowController.getWindowDimensions(), NIGHT_CYCLE_LENGTH);

        // Create Sun + Halo
        GameObject sun = Sun.create(gameObjects(), SUN_LAYER, windowController.getWindowDimensions(),
                SUN_CYCLE_LENGTH);

        SunHalo.create(gameObjects(), SUN_HALO_LAYER, sun, SUN_HALO_COLOR);

        ImagesManager imagesManager = new ImagesManager(imageReader);
        Renderable[] gameCards = imagesManager.getGameCards();
        AnimationRenderable fireworks = new AnimationRenderable(imagesManager.getFireworks(), 0.1);
        Vector2 halfWindow = windowController.getWindowDimensions().mult(0.5f);
        this.memoryGameManager = new MemoryGameManager(halfWindow, halfWindow, fireworks);
        memoryGameManager.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects().addGameObject(memoryGameManager, Layer.BACKGROUND);


        //avatar
        FloatCounter counter = new FloatCounter(MAXIMUM_ENERGY);
        Vector2 avatarInitCoords = new Vector2(windowController.getWindowDimensions().x()/2,
                terrain.groundHeightAt(windowController.getWindowDimensions().x()/2) - 3*Block.SIZE);
        Avatar avatar = Avatar.create(gameObjects(), AVATAR_LAYER, avatarInitCoords, inputListener,
                                      imageReader);
        avatar.setCounter(counter);

        this.mainAvatar = avatar;
        Camera camera = new Camera(Vector2.ZERO, windowController.getWindowDimensions(),
                windowController.getWindowDimensions());
        setCamera(camera);
        camera().setToFollow(avatar, Vector2.ZERO);
        gameObjects().layers().shouldLayersCollide(AVATAR_LAYER, TREES_LAYER, true);

        //Counter
        NumericEnergyCounter.createNumericEnergyCounter(Vector2.ONES.mult(15), ENERGY_COUNTER_SIZE,
                                                        gameObjects(), counter, ENERGY_COUNTER_TEXT,
                                                        ENERGY_COUNTER_LAYER);

        // --------- Create Trees -------
        // Make sure there won't be any trees created in the avatars location
        float[][] excludedXRanges = new float[][]{new float[]{avatarInitCoords.x(),
                                                        avatarInitCoords.x() + avatar.getDimensions().x()}};

        this.tree = new Tree(terrain::groundHeightAt, gameObjects(), TREES_LAYER, LEAVES_LAYER,
                seed, inputListener, gameCards, memoryGameManager.revealedCards, excludedXRanges);
        tree.createInRange((int) mainChunk[FROM], (int) mainChunk[TO]);
        gameObjects().layers().shouldLayersCollide(LEAVES_LAYER, TERRAIN_LAYER, true);
    }

    /**
     * In addition to the super update, updates the world to be infinite (creates and removes chuncks of the
     * world according to the avatar placement in the game)
     *
     * @param deltaTime game time
     */
    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        float avatarLeftCoordinate = mainAvatar.getTopLeftCorner().x();
        float avatarRightCoordinate = mainAvatar.getTopLeftCorner().x() + mainAvatar.getDimensions().x();

        // the case where the avatar nears the right border and there is no right chunk created
        if (avatarRightCoordinate > mainChunk[TO] - CHUNK_MARGIN &&
                avatarRightCoordinate <= mainChunk[TO] && rightChunk == null) {
            rightChunk = new float[RANGE_COORDINATES];
            rightChunk[FROM] = mainChunk[TO];
            rightChunk[TO] = rightChunk[FROM] + CHUNK_SIZE;
            createChunk(terrain, tree, rightChunk[FROM], rightChunk[TO]);
        }
        // the avatar crossed the margin of right chunk. Two things happen:
        // 1.the chunk on the left disappears. 2. the right chunk becomes the main one
        if (avatarLeftCoordinate > mainChunk[TO] + CHUNK_MARGIN) {
            if (leftChunk != null)
            {
                deleteChunk(leftChunk[FROM], leftChunk[TO]);
            }
            shiftChunksLeft();
        }

        if (avatarLeftCoordinate < mainChunk[FROM] + CHUNK_MARGIN &&
                avatarLeftCoordinate > mainChunk[FROM] && leftChunk == null) {
            leftChunk = new float[RANGE_COORDINATES];
            leftChunk[TO] = mainChunk[FROM];
            leftChunk[FROM] = leftChunk[TO] - CHUNK_SIZE;
            createChunk(terrain, tree, leftChunk[FROM], leftChunk[TO]);
        }
        // the avatar crossed the margin of left chunk. Two things happen:
        if (leftChunk != null && avatarRightCoordinate < leftChunk[TO] - CHUNK_MARGIN) {
            if (rightChunk != null)
            {
                deleteChunk(rightChunk[FROM], rightChunk[TO]);
            }
            shiftChunksRight();
        }
    }

    /**
     * Moves all the chunks to the right
     * The left one disappears, the main become the left one, and the right becomes the main one (where the
     * avatar is right now)
     */
    private void shiftChunksLeft(){
        leftChunk = mainChunk;
        mainChunk = rightChunk;
        rightChunk = null;
    }

    /**
     * Moves all the chunks to the left
     * The right one disappears, the main become the right one, and the left becomes the main one (where the
     * avatar is right now)
     */
    private void shiftChunksRight(){
        rightChunk = mainChunk;
        mainChunk = leftChunk;
        leftChunk = null;
    }

    /**
     * recreates a chunk of the game (terrain, trees)
     * @param from a chunk will be created from this x-coordinate
     * @param to a chunk will be created from this y-coordinate
     */
    private void createChunk(Terrain terrain, Tree tree, float from, float to){
        terrain.createInRange((int) from, (int) to);
        tree.createInRange((int) from,(int) to);
    }

    /**
     * Deletes every tree and terrain
     * @param from where to delete from (inclusive)
     * @param to until where to delete (exclusive)
     */
    private void deleteChunk(float from, float to){
        for(GameObject gameObject : gameObjects()) {
            float objectLeftSide = gameObject.getTopLeftCorner().x();
            //float objectRightSide = objectLeftSide + gameObject.getDimensions().x();
            if(objectLeftSide >= Math.floor(from/Block.SIZE) * Block.SIZE && objectLeftSide <= to){
                removeObject(gameObject);

                if (memoryGameManager.revealedCards.contains(gameObject)){
                    memoryGameManager.revealedCards.remove(gameObject);
                }
            }
        }
    }

    /**
     * deletes given object if it is in the layersToDelete array initialized in the constructor
     * @param gameObject the gameObject ro remove
     */
    private void removeObject(GameObject gameObject){
        for (int layer: layersToDelete) {
            gameObjects().removeGameObject(gameObject, layer);
        }
    }

    /**
     * Runs the entire simulation.
     *
     * @param args This argument should not be used.
     */
    public static void main(String[] args) {
        new PepseGameManager().run();
    }

}
