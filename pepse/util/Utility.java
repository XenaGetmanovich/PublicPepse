package pepse.util;

import pepse.world.Block;

public class Utility {
    private Utility() {}
    /**
     * calculates the closest integer to x divisible by block.size
     * @param x x coordinate.
     * @return the closest integer to x divisible by block.size
     */
    public static int findClosestDivisable(int x){
        return Block.SIZE*(Math.round(x/Block.SIZE));
    }

}
