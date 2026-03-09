package pepse.world.trees;

import danogl.collisions.GameObjectCollection;
import danogl.gui.UserInputListener;
import danogl.gui.rendering.RectangleRenderable;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;
import pepse.minigame.MemoryGameCard;
import pepse.world.Block;

import java.awt.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Random;
import java.util.function.Function;

/**
 * Responsible for the creation and management of trees.
 */
public class Tree {
    private static final Color TRUNK_COLOR = new Color(100, 50, 20);
    private static final int MINIMAL_TREE_HEIGHT = 5;
    private static final int MAXIMUM_TREE_HEIGHT = 12;
    private static final int TREE_PROBABILITY_DENOMIMNATOR = 10;
    private static final int TREE_PROBABILITY_NUMERATOR = 1;
    private static final int EXCLUDED_UPPER_BOUND = 1;
    private static final int EXCLUDED_LOWER_BOUND = 0;
    private final UserInputListener inputListener;
    private final HashSet<MemoryGameCard> revealedGameCards;
    private final Renderable[] gameCardsPool;
    private final Random cardSelector;
    private final Function<Float, Float> groundHeightAt;
    private final GameObjectCollection gameObjects;
    private final float[][] excludedXRanges;
    private final int leavesLayer;
    private final int trunksLayer;
    private final int seed;

    /**
     * Constructor.
     *
     * @param groundHeightAt A function which returns the ground height.
     * @param gameObjects The collection of all participating game objects.
     * @param trunksLayer The object layer in which to create the trees trunks.
     * @param leavesLayer The object layer in which to create the leaves.
     * @param seed A seed for a random number generator.
     * @param inputListener Contains a single method: isKeyPressed, which returns whether
*      *                    a given key is currently pressed by the user or not.
     * @param gameCardsPool All memory game card images.
     * @param revealedGameCards The cards that had already been revealed.
     * @param excludedXRanges X coordinates in which trees should not be created.
     *                        (for example: avatars initial location).
     */
    public Tree(Function<Float, Float> groundHeightAt, GameObjectCollection gameObjects, int trunksLayer,
                int leavesLayer, int seed, UserInputListener inputListener, Renderable[] gameCardsPool,
                HashSet<MemoryGameCard> revealedGameCards, float[][] excludedXRanges){
        this.groundHeightAt = groundHeightAt;
        this.gameObjects = gameObjects;
        this.trunksLayer = trunksLayer;
        this.seed = seed;
        this.inputListener = inputListener;
        this.revealedGameCards = revealedGameCards;
        this.gameCardsPool = gameCardsPool;
        this.cardSelector = new Random();
        this.excludedXRanges = excludedXRanges;
        this.leavesLayer = leavesLayer;
    }

    /**
     * This method creates trees in a given range of x-values.
     *
     * @param minX The lower bound of the given range (will be rounded to a multiple of Block.SIZE).
     * @param maxX The upper bound of the given range (will be rounded to a multiple of Block.SIZE).
     */
    public void createInRange(int minX, int maxX){
        // Match bounds to a multiplication of Block.SIZE
        int blockMinX = (int) (Math.floor(minX / Block.SIZE) * Block.SIZE);
        int blockMaxX = (int) (Math.floor(maxX / Block.SIZE) * Block.SIZE);

        Renderable treeTrunkRenderable = new RectangleRenderable(TRUNK_COLOR);

        // Go over range and create trees
        for (int i = blockMinX; i < blockMaxX - Block.SIZE ; i+= Block.SIZE) {
            Random random = new Random(Objects.hash(i, seed));

            boolean isTreeLocation = (random.nextInt(TREE_PROBABILITY_DENOMIMNATOR) <= TREE_PROBABILITY_NUMERATOR - 1);
            if (!isTreeLocation || isInExcludedRanges(i)){
                continue;
            }

            float groundHeight = groundHeightAt.apply((float) i);
            Vector2 treeLocation = new Vector2(i, groundHeight);

            int treeHeight =
                    random.nextInt(MAXIMUM_TREE_HEIGHT - MINIMAL_TREE_HEIGHT) + MINIMAL_TREE_HEIGHT;

            // Generate foliage size and make sure it is odd so it will be displayed nicer
            int foliageSize = treeHeight / 2 + random.nextInt(treeHeight / 2 + 1);
            foliageSize += (foliageSize % 2 == 0 ? 1 : 0);

            createTree(treeHeight, treeTrunkRenderable, foliageSize, treeLocation);
        }
    }

    /*
     * Checks if the x coordinate given mustn't contain a tree.
     */
    private boolean isInExcludedRanges(int x)
    {
        for (float[] range: excludedXRanges){
            if (range[EXCLUDED_LOWER_BOUND] - Block.SIZE <= x && x <= range[EXCLUDED_UPPER_BOUND]){
                return true;
            }
        }

        return false;
    }

    /*
     * Creates a single tree.
     */
    private void createTree(int treeHeight, Renderable trunkRenderable, int foliageSize, Vector2 location){
        // Create trunk
        for (int y = 1; y <= treeHeight; y++) {
            Vector2 blockTopLeft = new Vector2(location.x(), location.y() - y * Block.SIZE);
            Block trunkBlock = new Block(blockTopLeft, trunkRenderable);
            gameObjects.addGameObject(trunkBlock, trunksLayer);
        }

        // Create leaves
        int foliageLeft = (int) (location.x() - (foliageSize / 2) * Block.SIZE);
        int foliageBottom = (int) (location.y() + (-treeHeight + (foliageSize / 2)) * Block.SIZE);
        Random leafRandomizer = new Random(Objects.hash(location, seed));

        Leaf.createFoliage(Vector2.of(foliageLeft, foliageBottom), foliageSize, gameCardsPool, gameObjects,
                            revealedGameCards, leavesLayer, leafRandomizer);
    }
}
