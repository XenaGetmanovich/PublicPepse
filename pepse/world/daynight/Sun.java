package pepse.world.daynight;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.components.Transition;
import danogl.gui.rendering.OvalRenderable;
import danogl.util.Vector2;

import java.awt.*;

/**
 * Represents the sun - moves across the sky in an elliptical path.
 */
public class Sun {

    public static final Vector2 SUN_SIZE = new Vector2(100,100);
    public static final float SUN_AT_MIDDAY = 90;
    private static final String TAG = "sun";

    /**
     * This function creates a yellow circle that moves in the sky in an elliptical path
     * (in camera coordinates).
     *
     * @param gameObjects The collection of all participating game objects.
     * @param layer The number of the layer to which the created sun should be added.
     * @param windowDimensions The dimensions of the windows.
     * @param cycleLength The amount of seconds it should take the created game object to complete a full cycle.
     *
     * @return A new game object representing the sun.
     */
    public static GameObject create(GameObjectCollection gameObjects, int layer, Vector2 windowDimensions,
                                    float cycleLength) {
        Vector2 initial_sun_placement = new Vector2((float) (windowDimensions.x() * 0.5),
                                                    (float) (windowDimensions.y() * 0.5));

        OvalRenderable ovalRenderable = new OvalRenderable(Color.YELLOW);
        GameObject sun = new GameObject(Vector2.ZERO, SUN_SIZE, ovalRenderable);
        sun.setCenter(initial_sun_placement);
        sun.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        sun.setTag(TAG);
        gameObjects.addGameObject(sun, layer);
        Vector2 sun_moves_around = windowDimensions.mult(0.5f);
        float radiusX = windowDimensions.x() / 2;
        float radiusY = windowDimensions.y() / 2;
        new Transition<Float>(sun, angle -> sun.setCenter(new Vector2(
                (float) Math.cos(Math.toRadians(angle)) * radiusX,
                    (float) -Math.sin(Math.toRadians(angle)) * radiusY).add(sun_moves_around)),
                         SUN_AT_MIDDAY,
                SUN_AT_MIDDAY + 360f, Transition.LINEAR_INTERPOLATOR_FLOAT, cycleLength,
                Transition.TransitionType.TRANSITION_LOOP, null);

        return sun;
    }
}
