package pepse.util;

import danogl.gui.ImageReader;
import danogl.gui.rendering.Renderable;

/**
 * Groups all image paths in one place and is able to turn them into a renderable.
 */
public class ImagesManager {
    private final ImageReader imageReader;
    public String[][] animalsPathPool = new String[][]{
        new String[]{"assets/run/squirrel-run-1.png", "assets/run/squirrel-run-2.png",
                "assets/run/squirrel-run-3.png", "assets/run/squirrel-run-4.png",
                "assets/run/squirrel-run-5.png", "assets/run/squirrel-run-6.png"},
    };

    private Renderable[] animals;
    public String[] gameCardsPathPool = new String[]{"assets/memory cards/bigCyclop.png",
            "assets/memory cards/tiredEye.png", "assets/memory cards/blueFlower.png",
            "assets/memory cards/greenTurtle.png", "assets/memory cards/redTurtle.png",
            "assets/memory cards/blueTurtle.png", "assets/memory cards/eyes.png",
            "assets/memory cards/redFlower.png", "assets/memory cards/coin.png",
            "assets/memory cards/eye.png", "assets/memory cards/mario.png",
            "assets/memory cards/crazyEyes.png", "assets/memory cards/explosionCyclop.png",
            "assets/memory cards/luigi.png", "assets/memory cards/cute.png",
            "assets/memory cards/explosion.png", "assets/memory cards/littleCyclop.png",
            "assets/memory cards/egg.png","assets/memory cards/elegant.png"};
    private Renderable[] gameCards;
    private String[] fireworksFrames = new String[]{"assets/fireworks/30.png", "assets/fireworks/29.png",
            "assets/fireworks/28.png", "assets/fireworks/27.png", "assets/fireworks/26.png",
            "assets/fireworks/25.png", "assets/fireworks/24.png", "assets/fireworks/23.png",
            "assets/fireworks/22.png", "assets/fireworks/21.png", "assets/fireworks/20.png",
            "assets/fireworks/19.png", "assets/fireworks/18.png", "assets/fireworks/17.png",
            "assets/fireworks/16.png", "assets/fireworks/15.png", "assets/fireworks/14.png",
            "assets/fireworks/13.png", "assets/fireworks/12.png", "assets/fireworks/11.png",
            "assets/fireworks/10.png", "assets/fireworks/9.png", "assets/fireworks/8.png",
            "assets/fireworks/7.png", "assets/fireworks/6.png", "assets/fireworks/5.png",
            "assets/fireworks/4.png", "assets/fireworks/3.png", "assets/fireworks/2.png",
            "assets/fireworks/1.png"
    };
    private Renderable[] fireworks;

    public static String[] avatarWalkingFrames = new String[]{"assets/avatarMoves/walking/walking01.png",
            "assets/avatarMoves/walking/walking02.png", "assets/avatarMoves/walking/walking03.png",
            "assets/avatarMoves/walking/walking04.png", "assets/avatarMoves/walking/walking05.png",
            "assets/avatarMoves/walking/walking06.png", "assets/avatarMoves/walking/walking07.png",
            "assets/avatarMoves/walking/walking08.png", "assets/avatarMoves/walking/walking09.png",
            "assets/avatarMoves/walking/walking10.png", "assets/avatarMoves/walking/walking11.png",
            "assets/avatarMoves/walking/walking12.png", "assets/avatarMoves/walking/walking13.png",
            "assets/avatarMoves/walking/walking14.png"};

    public static String[] avatarJumpingFrames = new String[]{"assets/avatarMoves/jumping/jumping01.png",
            "assets/avatarMoves/jumping/jumping02.png", "assets/avatarMoves/jumping/jumping03.png",
            "assets/avatarMoves/jumping/jumping04.png", "assets/avatarMoves/jumping/jumping05.png",
            "assets/avatarMoves/jumping/jumping06.png", "assets/avatarMoves/jumping/jumping07.png",
            "assets/avatarMoves/jumping/jumping08.png", "assets/avatarMoves/jumping/jumping09.png",
            "assets/avatarMoves/jumping/jumping10.png", "assets/avatarMoves/jumping/jumping11.png",
            "assets/avatarMoves/jumping/jumping12.png", "assets/avatarMoves/jumping/jumping13.png",
            "assets/avatarMoves/jumping/jumping14.png", "assets/avatarMoves/jumping/jumping15.png",
            "assets/avatarMoves/jumping/jumping16.png"};

    public static String[] avatarFlyingFrames = new String[]{"assets/avatarMoves/flying/flying1.png",
            "assets/avatarMoves/flying/flying2.png", "assets/avatarMoves/flying/flying3.png",
            "assets/avatarMoves/flying/flying4.png", "assets/avatarMoves/flying/flying5.png"};

    public static String[] avatarStandingFrames = new String[]{ "assets/avatarMoves/idle/idle1.png",
            "assets/avatarMoves/idle/idle2.png", "assets/avatarMoves/idle/idle3.png", "assets/avatarMoves/idle/idle4.png",
            "assets/avatarMoves/idle/idle5.png", "assets/avatarMoves/idle/idle6.png", "assets/avatarMoves/idle/idle7.png",
            "assets/avatarMoves/idle/idle8.png", "assets/avatarMoves/idle/idle9.png"};

    /**
     * Constructor.
     *
     * @param imageReader Contains a single method: readImage, which reads an image from disk.
     */
    public ImagesManager(ImageReader imageReader){
        this.imageReader = imageReader;
    }

    /**
     * Returns available memory game cards images.
     * @return game cards images.
     */
    public Renderable[] getGameCards(){
        if (gameCards == null) {
            gameCards = new Renderable[gameCardsPathPool.length];
            loadImageArray(gameCardsPathPool, gameCards, false);
        }

        return gameCards;
    }

    public Renderable[] getAnimals(){
        for (String[] animal: animalsPathPool)
            loadImageArray(animal, animals, true);

        return animals;
    }

    /**
     * Returns fireworks animation images.
     * @return fireworks images.
     */
    public Renderable[] getFireworks(){
        if (fireworks == null) {
            fireworks = new Renderable[fireworksFrames.length];
            loadImageArray(fireworksFrames, fireworks, true);
        }

        return fireworks;
    }


    public Renderable[] getAvatarWalk(){
        Renderable[] walk = new Renderable[avatarWalkingFrames.length];
        loadImageArray(avatarWalkingFrames, walk, true);

        return walk;
    }

    public Renderable[] getAvatarJump(){
        Renderable[] jump = new Renderable[avatarJumpingFrames.length];
        loadImageArray(avatarJumpingFrames, jump, true);

        return jump;
    }

    public Renderable[] getAvatarFly(){
        Renderable[] fly = new Renderable[avatarFlyingFrames.length];
        loadImageArray(avatarFlyingFrames, fly, true);

        return fly;
    }

    public Renderable[] getAvatarStand(){
        Renderable[] stand = new Renderable[avatarStandingFrames.length];
        loadImageArray(avatarStandingFrames, stand, true);

        return stand;
    }

    /*
     * Reads all images mentioned in pathPool.
     */
    private void loadImageArray(String[] pathPool, Renderable[] images, boolean isTopLeftPixelTransparency){
            for (int i=0; i<pathPool.length; i++) {
                images[i] = imageReader.readImage(pathPool[i], isTopLeftPixelTransparency);
            }
    }
}
