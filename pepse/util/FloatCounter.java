package pepse.util;

/**
 * A class that represents an object that keeps track of a float value
 * Used as energy Counter
 */
public class FloatCounter{
    float counter;

    /**
     * Constructor
     * @param value set the initial value to be the given value
     */
    public FloatCounter(float value){
        counter = value;
    }

    /**
     * increases the value of the counter by a given value
     * @param value value to increase by
     */
    public void increaseBy(float value){
        counter += value;
    }

    /**
     * decreases the value of the counter by a given value
     * @param value value to decrease by
     */
    public void decreaseBy (float value){
        counter -= value;
    }

    /**
     * sets the value of the counter to be a certain value
     * @param value the new counter value
     */
    public void setValue (float value){
        counter = value;
    }

    /**
     * @return the value of the counter
     */
    public float value(){
        return counter;
    }
}
