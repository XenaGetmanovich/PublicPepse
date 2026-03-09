package pepse.world;

import danogl.GameObject;
import danogl.components.GameObjectPhysics;
import danogl.gui.rendering.Renderable;
import danogl.util.Vector2;

/**
 * Represents a single block (larger objects can be created from blocks).
 */
public class Block extends GameObject {
    /**
     * Size of a single block.
     */
    public static final int SIZE = 30;

    /**
     * Constructor.
     *
     * @param topLeftCorner The location of the top-left corner of the created block.
     * @param renderable A renderable to render as the block.
     */
    public Block(Vector2 topLeftCorner, Renderable renderable) {
        super(topLeftCorner, Vector2.ONES.mult(SIZE), renderable);
        physics().preventIntersectionsFromDirection(Vector2.ZERO);
        physics().setMass(GameObjectPhysics.IMMOVABLE_MASS);
    }


}
