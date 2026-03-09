package pepse.world;

import danogl.collisions.GameObjectCollection;
import danogl.gui.rendering.RectangleRenderable;
import danogl.util.Vector2;
import pepse.util.PerlinNoise;
import pepse.util.ColorSupplier;

import java.awt.*;

/**
 * Responsible for the creation and management of terrain.
 */
public class Terrain {
    private static final Color BASE_GROUND_COLOR = new Color(212, 123, 74);
    private static final int TERRAIN_DEPTH = 20;
    private static final String TAG = "ground";
    private static final float NOISE_FACTOR = 200;
    private static final float ZERO_GROUND_RATIO = (float) 2 / 3;
    private static final int COLLISIONABLE_ROWS = 2;
    private final PerlinNoise perlinNoise;
    private final GameObjectCollection gameObjects;
    private final int groundLayer;
    private final float zeroGroundHeightAtX;
    private final int nonCollisionableGroundLayer;

    /**
     * Constructor.
     *
     * @param gameObjects The collection of all participating game objects.
     * @param groundLayer The number of the layer to which the created ground objects should be added.
     * @param windowDimensions The dimensions of the windows.
     * @param seed A seed for a random number generator.
     */
    public Terrain(GameObjectCollection gameObjects, int groundLayer, Vector2 windowDimensions, int seed) {
        this.gameObjects = gameObjects;
        this.groundLayer = groundLayer;
        this.nonCollisionableGroundLayer = groundLayer + 1;
        this.zeroGroundHeightAtX = windowDimensions.y() * ZERO_GROUND_RATIO;
        this.perlinNoise = new PerlinNoise(seed);
    }

    /**
     * This method creates terrain in a given range of x-values.
     *
     * @param x A number.
     * @return The ground height at the given location.
     */
    public float groundHeightAt(float x) {
        float noise = (float) perlinNoise.noise(x);
        return (int)((zeroGroundHeightAtX + noise * NOISE_FACTOR) / Block.SIZE) * Block.SIZE;
    }

    /**
     * This method creates terrain in a given range of x-values.
     *
     * @param minX The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void createInRange(int minX, int maxX) {
        int trueMinX = (int) (Math.floor(minX / Block.SIZE) * Block.SIZE);

        for (int x = trueMinX; x <= maxX; x += Block.SIZE) {
            int trueY = (int) groundHeightAt(x);

            // Collisionable terrain
            for (int yOffset = 0; yOffset < COLLISIONABLE_ROWS * Block.SIZE; yOffset+=Block.SIZE) {
                RectangleRenderable rectangleRenderable = new
                        RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));

                Block block = new Block(new Vector2(x, trueY + yOffset), rectangleRenderable);
                block.setTag(TAG);
                gameObjects.addGameObject(block, groundLayer);
            }

            // Non-collisionable terrain
            for (int yOffset = Block.SIZE; yOffset < TERRAIN_DEPTH * Block.SIZE; yOffset+=Block.SIZE) {
                RectangleRenderable rectangleRenderable = new
                        RectangleRenderable(ColorSupplier.approximateColor(BASE_GROUND_COLOR));

                Block block = new Block(new Vector2(x, trueY + yOffset), rectangleRenderable);
                gameObjects.addGameObject(block, nonCollisionableGroundLayer);
            }
        }
    }
}
