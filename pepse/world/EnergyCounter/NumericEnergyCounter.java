package pepse.world.EnergyCounter;

import danogl.GameObject;
import danogl.collisions.GameObjectCollection;
import danogl.components.CoordinateSpace;
import danogl.gui.rendering.Renderable;
import danogl.gui.rendering.TextRenderable;
import danogl.util.Vector2;
import pepse.util.FloatCounter;

/**
 * Numeric counter that will show the energy level on the screen
 */
public class NumericEnergyCounter extends GameObject {
    private FloatCounter counter;
    float prevEnergyLevel;

    private String counterText;

    public NumericEnergyCounter(Vector2 topLeftCorner, Vector2 dimensions, Renderable renderable) {
        super(topLeftCorner, dimensions, renderable);
        this.renderer().setRenderable(new TextRenderable(counterText));
    }

    static public NumericEnergyCounter
    createNumericEnergyCounter(Vector2 topLeftCorner, Vector2 dimensions, GameObjectCollection gameObjects,
                               FloatCounter floatCounter, String counterText, int layer){
        Renderable renderable = new TextRenderable(counterText);
        NumericEnergyCounter numericCounter = new NumericEnergyCounter(topLeftCorner, dimensions, renderable);
        numericCounter.setTag("counter");
        numericCounter.renderer().setRenderable(renderable);
        numericCounter.counter = floatCounter;
        numericCounter.counterText = counterText;
        numericCounter.prevEnergyLevel = floatCounter.value();
        numericCounter.setCoordinateSpace(CoordinateSpace.CAMERA_COORDINATES);
        gameObjects.addGameObject(numericCounter, layer);
        return numericCounter;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        TextRenderable textRenderable = (TextRenderable) this.renderer().getRenderable();
        textRenderable.setString(counterText + Math.round(counter.value()));

    }
}
